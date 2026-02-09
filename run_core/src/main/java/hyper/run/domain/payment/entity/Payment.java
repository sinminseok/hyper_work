package hyper.run.domain.payment.entity;

import hyper.run.domain.common.BaseTimeEntity;
import hyper.run.domain.game.entity.Game;
import hyper.run.domain.payment.event.PaymentCreatedEvent;
import hyper.run.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 결제 내역
 */
@Table(name = "payment")
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment extends BaseTimeEntity<Payment> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", updatable = false)
    private Long id;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "coupon_amount", nullable = true)
    private int couponAmount;

    @Column(name = "in_app_type", nullable = false)
    private InAppType inAppType;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false, length = 20)
    private PaymentState state;

    @Column(name = "payment_method", nullable = true)
    private String paymentMethod;

    @Column(name = "transaction_id", nullable = false, unique = true)
    private String transactionId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "receipt_data", nullable = false, columnDefinition = "TEXT")
    private String receiptData;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Payment(int price, int couponAmount, InAppType inAppType, PaymentState state, String paymentMethod, String transactionId, String productId, String receiptData, User user) {
        this.price = price;
        this.couponAmount = couponAmount;
        this.inAppType = inAppType;
        this.state = state;
        this.paymentMethod = paymentMethod;
        this.transactionId = transactionId;
        this.productId = productId;
        this.receiptData = receiptData;
        this.user = user;
    }

    @PostPersist
    private void publishPaymentCreatedEvent() {
        registerEvent(new PaymentCreatedEvent(this.id, this.user.getId()));
    }

    public void updateState(PaymentState updateState) {
        this.state = updateState;
    }
}
