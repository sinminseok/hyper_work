package hyper.run.domain.payment.event;

import hyper.run.common.enums.JobType;
import hyper.run.common.job.JobEventPayload;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaymentJobPayload extends JobEventPayload {

    private Long paymentId;
    private Long userId;

    public PaymentJobPayload(Long paymentId, Long userId) {
        this.paymentId = paymentId;
        this.userId = userId;
    }

    @Override
    public JobType getType() {
        return JobType.PAYMENT_CREATED;
    }
}
