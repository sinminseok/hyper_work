package hyper.run.domain.payment.application;

import hyper.run.common.job.JobProcessor;
import hyper.run.common.enums.JobType;
import hyper.run.domain.outbox.entity.OutboxEvent;
import hyper.run.domain.payment.event.PaymentCreatedMessage;
import hyper.run.domain.outbox.repository.OutboxEventRepository;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import hyper.run.domain.payment.repository.PaymentRepository;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import io.awspring.cloud.sqs.listener.Visibility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Payment 생성 이벤트를 처리하는 Processor
 * - PaymentCreatedMessage를 받아 쿠폰 지급 및 Payment 상태 변경
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentCreatedJobProcessor implements JobProcessor<PaymentCreatedMessage> {

    private final OutboxEventRepository outboxEventRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void process(PaymentCreatedMessage message, Visibility visibility) {
        String outboxEventId = message.getOutboxEventId();

        try {
            // STEP 1: OutboxEvent 조회 및 멱등성 체크
            OutboxEvent outboxEvent = outboxEventRepository.findById(outboxEventId)
                    .orElseThrow(() -> new IllegalStateException("OutboxEvent not found: " + outboxEventId));

            if (outboxEvent.isPublished()) {
                return;
            }

            // STEP 2: Payment 조회 및 상태 확인
            Payment payment = paymentRepository.findById(message.getPaymentId())
                    .orElseThrow(() -> new IllegalStateException("Payment not found: " + message.getPaymentId()));

            if (payment.getState() != PaymentState.PENDING) {
                outboxEvent.publish();
                return;
            }

            // STEP 3: User 쿠폰 지급
            User user = userRepository.findById(message.getUserId())
                    .orElseThrow(() -> new IllegalStateException("User not found: " + message.getUserId()));

            if (message.getCouponAmount() > 0) {
                user.increaseCouponByAmount(message.getCouponAmount());
            } else {
                user.increaseCoupon();
            }

            // STEP 4: Payment 상태 변경
            payment.updateState(PaymentState.PAYMENT_COMPLETED);

            // STEP 5: OutboxEvent 완료 처리
            outboxEvent.publish();

        } catch (Exception e) {
            log.error("Failed to process payment. outboxEventId: {}, paymentId: {}", outboxEventId, message.getPaymentId(), e);
            throw e;
        }
    }

    @Override
    public JobType getType() {
        return JobType.PAYMENT_CREATED;
    }
}
