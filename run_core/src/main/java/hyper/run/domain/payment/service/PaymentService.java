package hyper.run.domain.payment.service;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.repository.CustomerInquiryRepository;
import hyper.run.domain.payment.dto.request.PaymentRequest;
import hyper.run.domain.payment.dto.request.PaymentSearchRequest;
import hyper.run.domain.payment.dto.response.AdminPaymentResponse;
import hyper.run.domain.payment.dto.response.PaymentResponse;
import hyper.run.domain.inquiry.dto.response.RefundPaymentResponse;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import hyper.run.domain.payment.repository.PaymentRepository;
import hyper.run.domain.payment.repository.impl.PaymentCustomRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.stream.Collectors;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_EMAIL;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository repository;
    private final UserRepository userRepository;
    private final PaymentCustomRepository paymentCustomRepository;
    private final CustomerInquiryRepository customerInquiryRepository;

    /**
     * 결제 메서드
     */
    public void pay(final String email, final PaymentRequest request){
        User user = OptionalUtil.getOrElseThrow(userRepository.findByEmail(email), NOT_EXIST_USER_EMAIL);
        Payment payment = request.toEntity(user);
        user.addPayment(payment);
        repository.save(payment); // Payment 저장
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
     * 결제 목록 필터링을 통해 모두 조회
     */
    public Page<AdminPaymentResponse> searchPayments(final PaymentSearchRequest searchRequest, Pageable pageable){
        return paymentCustomRepository.searchPayments(searchRequest,pageable);
    }

    /**
     * 환불 조회시 사용자 정보까지 조회
     */
    public RefundPaymentResponse getRefundPayment(final Long paymentId){
        Payment payment = repository.getReferenceById(paymentId);
        CustomerInquiry inquiry = OptionalUtil.getOrElseThrow(customerInquiryRepository.findByPaymentId(paymentId),"존재하지 않는 문의 사항입니다.");
        return RefundPaymentResponse.paymentToRefundDto(payment,inquiry);
    }

    /**
     * 환불 요청 시 승인 및 취소
     */
    @Transactional
    public void confirmRefund(final Long paymentId){
        Payment payment = OptionalUtil.getOrElseThrow(repository.findById(paymentId),"존재하지 않는 결제건입니다.");
        payment.setState(PaymentState.REFUND_COMPLETED);
    }
    @Transactional
    public void rejectRefund(final Long paymentId){
        Payment payment = OptionalUtil.getOrElseThrow(repository.findById(paymentId),"존재하지 않는 결제건입니다.");
        payment.setState(PaymentState.REFUND_REJECTED);
    }
}
