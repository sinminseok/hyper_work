package hyper.run.domain.payment.consumer;

import hyper.run.common.enums.JobType;
import hyper.run.common.job.JobProcessor;
import hyper.run.common.job.JobProcessorFactory;
import hyper.run.domain.payment.event.PaymentCreatedMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnProperty(name = "cloud.aws.sqs.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
public class PaymentCreatedConsumer {

    private final JobProcessorFactory jobProcessorFactory;

    @SqsListener(value = "${cloud.aws.sqs.queue.payment-created}", messageVisibilitySeconds = "300")
    public void consume(PaymentCreatedMessage message) {
        JobProcessor<PaymentCreatedMessage> processor = jobProcessorFactory.getProcessor(JobType.PAYMENT_CREATED);
        processor.process(message, null);
    }

}
