package hyper.run.domain.payment.event;

import lombok.Getter;

@Getter
public class PaymentCreatedEvent {
    private final Long paymentId;
    private final Long userId;

    public PaymentCreatedEvent(Long paymentId, Long userId) {
        this.paymentId = paymentId;
        this.userId = userId;
    }
}
