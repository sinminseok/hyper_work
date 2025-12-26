package hyper.run.domain.payment.producer;

import hyper.run.domain.outbox.application.OutboxEventPublishProcessor;
import hyper.run.common.enums.JobType;
import hyper.run.common.job.JobEventPayload;
import hyper.run.domain.payment.event.PaymentJobPayload;
import hyper.run.domain.payment.event.PaymentCreatedMessage;
import hyper.run.utils.SnsPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCreatedProcessor extends OutboxEventPublishProcessor {

    private final SnsPublisherService snsPublisherService;

    @Override
    protected void publish(String eventId, JobEventPayload data) {
        PaymentJobPayload paymentData = (PaymentJobPayload) data;
        PaymentCreatedMessage message = PaymentCreatedMessage.from(eventId, paymentData);
        snsPublisherService.publish(JobType.PAYMENT_CREATED, message, eventId);
    }

    @Override
    protected JobType getType() {
        return JobType.PAYMENT_CREATED;
    }
}
