package hyper.run.domain.payment.consumer;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.repository.CustomerInquiryRepository;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import hyper.run.domain.payment.event.RefundApprovedEvent;
import hyper.run.domain.payment.event.RefundRejectedEvent;
import hyper.run.domain.payment.repository.PaymentRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.exception.ErrorMessages;
import hyper.run.utils.EmailService;
import hyper.run.utils.OptionalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class RefundListener {

    private final PaymentRepository paymentRepository;
    private final CustomerInquiryRepository customerInquiryRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleRefundApprovedStateChange(RefundApprovedEvent event) {
        Payment payment = OptionalUtil.getOrElseThrow(paymentRepository.findById(event.getPaymentId()), ErrorMessages.NOT_EXIST_PAYMENT_ID);
        payment.setState(PaymentState.REFUND_COMPLETED);

        CustomerInquiry inquiry = OptionalUtil.getOrElseThrow(customerInquiryRepository.findById(event.getInquiryId()), ErrorMessages.NOT_EXIST_INQUIRY_ID);
        inquiry.approveRefund(event.getReason());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRefundApprovedEmail(RefundApprovedEvent event) {
        try {
            String subject = "[FunnyRun] 환불 요청이 승인되었습니다.";
            String emailBody = String.format(
                    "안녕하세요, %s님.\n\n" +
                            "요청하신 환불이 승인되었습니다.\n\n" +
                            "환불 금액: %,d원\n" +
                            "입금 계좌: %s %s\n" +
                            "승인 사유: %s\n\n" +
                            "영업일 기준 3-5일 이내에 환불 처리됩니다.\n\n" +
                            "감사합니다.\n" +
                            "FunnyRun 고객지원팀",
                    event.getUserName(),
                    event.getRefundPrice(),
                    event.getBankName(),
                    event.getAccountNumber(),
                    event.getReason()
            );

            emailService.sendSimpleEmail(event.getUserEmail(), subject, emailBody);
        } catch (Exception e) {
            log.error("Failed to send refund approved email. userEmail: {}", event.getUserEmail(), e);
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handleRefundRejectedStateChange(RefundRejectedEvent event) {
        Payment payment = OptionalUtil.getOrElseThrow(paymentRepository.findById(event.getPaymentId()), ErrorMessages.NOT_EXIST_PAYMENT_ID);
        payment.setState(PaymentState.REFUND_REJECTED);

        CustomerInquiry inquiry = OptionalUtil.getOrElseThrow(customerInquiryRepository.findById(event.getInquiryId()), ErrorMessages.NOT_EXIST_INQUIRY_ID);
        inquiry.rejectRefund(event.getReason());

        User user = OptionalUtil.getOrElseThrow(userRepository.findByIdForUpdate(event.getUserId()), ErrorMessages.NOT_EXIST_USER_ID);
        user.increaseCouponByAmount(event.getCouponAmount());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRefundRejectedEmail(RefundRejectedEvent event) {
        try {
            String subject = "[FunnyRun] 환불 요청 처리 결과 안내";
            String emailBody = String.format(
                    "안녕하세요, %s님.\n\n" +
                            "요청하신 환불이 거절되었습니다.\n\n" +
                            "거절 사유: %s\n\n" +
                            "차감되었던 쿠폰 %d개가 복구되었습니다.\n\n" +
                            "추가 문의사항이 있으시면 언제든지 연락 주시기 바랍니다.\n\n" +
                            "감사합니다.\n" +
                            "FunnyRun 고객지원팀",
                    event.getUserName(),
                    event.getReason(),
                    event.getCouponAmount()
            );

            emailService.sendSimpleEmail(event.getUserEmail(), subject, emailBody);
        } catch (Exception e) {
            log.error("Failed to send refund rejected email. userEmail: {}", event.getUserEmail(), e);
        }
    }
}
