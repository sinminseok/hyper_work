package hyper.run.domain.inquiry.consumer;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import hyper.run.domain.inquiry.event.CommonInquiryCreatedEvent;
import hyper.run.domain.inquiry.event.RefundInquiryCreatedEvent;
import hyper.run.domain.inquiry.repository.CustomerInquiryRepository;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import hyper.run.domain.payment.repository.PaymentRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static hyper.run.exception.ErrorMessages.NOT_EXIST_PAYMENT_ID;
import static hyper.run.exception.ErrorMessages.NOT_EXIST_USER_ID;

/**
 * 문의 도메인 이벤트 리스너
 * - 환불 문의와 일반 문의를 각각 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryListener {

    private final CustomerInquiryRepository customerInquiryRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 환불 문의 생성 이벤트 처리
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleRefundInquiryCreated(RefundInquiryCreatedEvent event) {
        Payment payment = OptionalUtil.getOrElseThrow(paymentRepository.findByIdForUpdate(event.getPaymentId()), NOT_EXIST_PAYMENT_ID);
        User user = OptionalUtil.getOrElseThrow(userRepository.findByIdForUpdate(event.getUserId()), NOT_EXIST_USER_ID);

        user.validateRefundPossible(payment.getCouponAmount());
        user.decreaseCouponByAmount(payment.getCouponAmount());
        payment.updateState(PaymentState.REFUND_REQUESTED);

        // STEP 5: CustomerInquiry 저장
        CustomerInquiry inquiry = CustomerInquiry.builder()
                .email(event.getEmail())
                .user(user)
                .title(event.getTitle())
                .paymentId(event.getPaymentId())
                .type(InquiryType.REFUND)
                .state(InquiryState.WAITING)
                .refundPrice(event.getRefundPrice())
                .refundType(event.getRefundType())
                .accountNumber(event.getAccountNumber())
                .bankName(event.getBankName())
                .message(event.getMessage())
                .build();

        customerInquiryRepository.save(inquiry);
    }

    /**
     * 일반 문의 생성 이벤트 처리
     * - User 조회
     * - CustomerInquiry 저장
     */
    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleCommonInquiryCreated(CommonInquiryCreatedEvent event) {
        // STEP 1: User 조회
        User user = OptionalUtil.getOrElseThrow(
                userRepository.findById(event.getUserId()),
                NOT_EXIST_USER_ID
        );

        // STEP 2: CustomerInquiry 저장
        CustomerInquiry inquiry = CustomerInquiry.builder()
                .user(user)
                .title(event.getTitle())
                .email(event.getEmail())
                .state(InquiryState.WAITING)
                .type(event.getInquiryType())
                .message(event.getMessage())
                .build();

        customerInquiryRepository.save(inquiry);
    }
}
