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
    private int price;
    private int couponAmount;
    private String inAppType;
    private String paymentMethod;
    private String transactionId;
    private String productId;
    private String receiptData;

    public PaymentJobPayload(Long paymentId, Long userId, int price, int couponAmount, String inAppType, String paymentMethod, String transactionId, String productId, String receiptData) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.price = price;
        this.couponAmount = couponAmount;
        this.inAppType = inAppType;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.productId = productId;
        this.receiptData = receiptData;
    }

    @Override
    public JobType getType() {
        return JobType.PAYMENT_CREATED;
    }
}
