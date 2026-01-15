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
import hyper.run.utils.OptionalUtil;
import io.awspring.cloud.sqs.listener.Visibility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static hyper.run.exception.ErrorMessages.*;


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
        OutboxEvent outboxEvent = OptionalUtil.getOrElseThrow(outboxEventRepository.findById(outboxEventId), NOT_EXIST_OUTBOX_EVENT_ID);
        if (outboxEvent.isPublished()) {
            return;
        }

        Payment payment = OptionalUtil.getOrElseThrow(paymentRepository.findByIdForUpdate(message.getPaymentId()), NOT_EXIST_PAYMENT_ID);

        //결제 이벤트 중복 발행시 publish 처리 후 return 하기 위한 코드 (2차 검증용)
        if (payment.getState() != PaymentState.PENDING) {
            outboxEvent.publish();
            return;
        }

        User user = OptionalUtil.getOrElseThrow(userRepository.findByIdForUpdate(message.getPaymentId()), NOT_EXIST_USER_ID);
        user.increaseCouponByAmount(payment.getCouponAmount());

        payment.updateState(PaymentState.PAYMENT_COMPLETED);

        outboxEvent.publish();
    }

    @Override
    public JobType getType() {
        return JobType.PAYMENT_CREATED;
    }
}
