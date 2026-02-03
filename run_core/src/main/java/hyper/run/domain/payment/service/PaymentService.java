package hyper.run.domain.payment.service;

import hyper.run.annotation.DistributionLock;
import hyper.run.domain.payment.dto.request.PaymentRequest;

import hyper.run.domain.payment.dto.response.PaymentResponse;
import hyper.run.domain.payment.entity.InAppType;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import hyper.run.domain.payment.repository.PaymentRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.text.html.Option;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_EMAIL;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AppleReceiptService appleReceiptService;
    private final GoogleReceiptService googleReceiptService;
    private final PaymentRepository repository;
    private final UserRepository userRepository;

    /**
     * 결제 메서드
     * 1. 영수증 검증 (Apple/Google)
     * 2. 결제 정보 저장
     */
    @Transactional
    public void pay(final Long userId, final PaymentRequest request){
        //todo 운영 환경 전에 주석 해지
        //validateReceipt(request);
        User user = OptionalUtil.getOrElseThrow(userRepository.findByIdForUpdate(userId), NOT_EXIST_USER_EMAIL);
        Payment payment = request.toEntity(user);
        repository.save(payment);
    }

    /**
     * 영수증 검증 메서드
     * Apple/Google 서버와 통신하여 실제 결제 여부를 확인합니다.
     */
    private void validateReceipt(PaymentRequest request) {
        if(request.getInAppType().equals(InAppType.APPLE)){
            // Apple 영수증 검증
            appleReceiptService.verifyReceipt(
                    request.getTransactionId(),
                    request.getProductId(),
                    request.getReceiptData()
            );
        }
        else if(request.getInAppType().equals(InAppType.GOOGLE)){
            // Google 영수증 검증 (구현 예정)
            googleReceiptService.verifyReceipt(
                    request.getTransactionId(),
                    request.getProductId(),
                    request.getReceiptData()
            );
        }
    }


    /**
     * 환불 가능한 결제 내역 모두 조회 메서드
     */
    public List<PaymentResponse> findPossibleRefundPayment(final String email) {
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        return user.getPayments().stream()
                .filter(payment -> payment.getState() == PaymentState.PAYMENT_COMPLETED) //결제 완료된 내역들만
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
