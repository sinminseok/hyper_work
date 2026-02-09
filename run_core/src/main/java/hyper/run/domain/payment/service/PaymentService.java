package hyper.run.domain.payment.service;

import hyper.run.domain.payment.dto.request.PaymentRequest;

import hyper.run.domain.payment.dto.response.PaymentResponse;
import hyper.run.domain.payment.entity.InAppType;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import hyper.run.domain.payment.repository.PaymentRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.exception.custom.DuplicatedTransactionException;
import hyper.run.utils.OptionalUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_EMAIL;

@Slf4j
@Service
public class PaymentService {

    private final AppleReceiptService appleReceiptService;
    private final Optional<GoogleReceiptService> googleReceiptService;
    private final PaymentRepository repository;
    private final UserRepository userRepository;
    private final TransactionLockManager lockManager;

    public PaymentService(AppleReceiptService appleReceiptService,
                          Optional<GoogleReceiptService> googleReceiptService,
                          PaymentRepository repository,
                          UserRepository userRepository,
                          TransactionLockManager lockManager) {
        this.appleReceiptService = appleReceiptService;
        this.googleReceiptService = googleReceiptService;
        this.repository = repository;
        this.userRepository = userRepository;
        this.lockManager = lockManager;
    }

    /**
     * 결제 메서드
     * 1. transactionId 기반 락 획득 (동시성 제어)
     * 2. 중복 결제 체크
     * 3. 영수증 검증 (Apple/Google)
     * 4. 결제 정보 저장
     */
    @Transactional
    public void pay(final Long userId, final PaymentRequest request){
        String transactionId = request.getTransactionId();
        Lock lock = lockManager.getLock(transactionId);

        lock.lock();
        try {
            // 중복 결제 체크 (첫 번째 방어선)
            if (repository.existsByTransactionId(transactionId)) {
                log.warn("중복 결제 시도 감지: userId={}, transactionId={}", userId, transactionId);
                throw new DuplicatedTransactionException("이미 처리된 결제입니다. transactionId: " + transactionId);
            }

            validateReceipt(request);

            User user = OptionalUtil.getOrElseThrow(userRepository.findByIdForUpdate(userId), NOT_EXIST_USER_EMAIL);
            Payment payment = request.toEntity(user);

            try {
                repository.save(payment);
            } catch (DataIntegrityViolationException e) {
                // UNIQUE 제약조건 위반 시 (두 번째 방어선)
                log.error("중복 결제 DB 제약조건 위반: userId={}, transactionId={}", userId, transactionId, e);
                throw new DuplicatedTransactionException("이미 처리된 결제입니다. transactionId: " + transactionId);
            }
        } finally {
            lock.unlock();
            lockManager.releaseLock(transactionId);
            log.debug("락 해제: transactionId={}", transactionId);
        }
    }


    private void validateReceipt(PaymentRequest request) {
        if(request.getInAppType().equals(InAppType.APPLE)){
            appleReceiptService.verifyReceipt(
                    request.getTransactionId(),
                    request.getProductId(),
                    request.getReceiptData()
            );
        }
        else if(request.getInAppType().equals(InAppType.GOOGLE)){
            googleReceiptService.ifPresentOrElse(
                    service -> service.verifyReceipt(
                            request.getTransactionId(),
                            request.getProductId(),
                            request.getReceiptData()
                    ),
                    () -> {
                        throw new UnsupportedOperationException(
                                "Google Play 영수증 검증 서비스가 설정되지 않았습니다. " +
                                "google.play.enabled=true로 설정하고 Service Account JSON 키를 추가하세요."
                        );
                    }
            );
        }
    }


    /**
     * 환불 가능한 결제 내역 모두 조회 메서드
     */
    public List<PaymentResponse> findPossibleRefundPayment(final String email) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        return user.getPayments().stream()
                .filter(payment -> payment.getState() == PaymentState.PAYMENT_COMPLETED)
                .map(PaymentResponse::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 자신의 모든 결제 내역을 조회하는 메서드
     */
    public List<PaymentResponse> findAllByEmail(final String email){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        return user.getPayments().stream()
                .sorted(Comparator.comparing(Payment::getCreateDateTime).reversed())
                .map(PaymentResponse::toResponse)
                .collect(Collectors.toList());
    }


}
