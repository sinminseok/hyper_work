package hyper.run.domain.payment.event;

import hyper.run.common.enums.JobType;
import hyper.run.common.message.SqsMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreatedMessage implements SqsMessage {

    private String outboxEventId;

    private Long paymentId;

    private Long userId;

    public static PaymentCreatedMessage from(String outboxEventId, PaymentJobPayload data) {
        return PaymentCreatedMessage.builder()
                .outboxEventId(outboxEventId)
                .paymentId(data.getPaymentId())
                .userId(data.getUserId())
                .build();
    }

    @Override
    public JobType getJobType() {
        return JobType.PAYMENT_CREATED;
    }
}
