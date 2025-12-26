package hyper.run.domain.payment.event;

import hyper.run.domain.payment.entity.Payment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentCreatedEvent {

    private final Long paymentId;
    private final Long userId;
    private final int price;
    private final int couponAmount;
    private final String inAppType;
    private final String paymentMethod;
    private final String transactionId;
    private final String productId;
    private final String receiptData;
    private final LocalDateTime occurredAt;

    public PaymentCreatedEvent(Long paymentId, Long userId, int price, int couponAmount, String inAppType, String paymentMethod, String transactionId, String productId, String receiptData) {
        this.paymentId = paymentId;
        this.userId = userId;
        this.price = price;
        this.couponAmount = couponAmount;
        this.inAppType = inAppType;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.productId = productId;
        this.receiptData = receiptData;
        this.occurredAt = LocalDateTime.now();
    }

    public static PaymentCreatedEvent from(Payment payment) {
        return new PaymentCreatedEvent(
                payment.getId(),
                payment.getUser().getId(),
                payment.getPrice(),
                payment.getCouponAmount(),
                payment.getInAppType().name(),
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getProductId(),
                payment.getReceiptData()
        );
    }
}
