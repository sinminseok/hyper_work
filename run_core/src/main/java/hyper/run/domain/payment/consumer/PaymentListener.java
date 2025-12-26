package hyper.run.domain.payment.consumer;

import hyper.run.domain.payment.event.PaymentJobPayload;
import hyper.run.domain.outbox.entity.OutboxEvent;
import hyper.run.common.enums.JobType;
import hyper.run.domain.outbox.repository.OutboxEventRepository;
import hyper.run.domain.payment.event.PaymentCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentListener {

    private final OutboxEventRepository outboxEventRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void handlePaymentCreated(PaymentCreatedEvent event) {
        PaymentJobPayload paymentData = createOutboxEvent(event);
        OutboxEvent outboxEvent = new OutboxEvent(JobType.PAYMENT_CREATED, paymentData);
        outboxEventRepository.save(outboxEvent);
    }

    private PaymentJobPayload createOutboxEvent(PaymentCreatedEvent event){
        return new PaymentJobPayload(
                event.getPaymentId(),
                event.getUserId(),
                event.getPrice(),
                event.getCouponAmount(),
                event.getInAppType(),
                event.getPaymentMethod(),
                event.getTransactionId(),
                event.getProductId(),
                event.getReceiptData()
        );
    }
}
