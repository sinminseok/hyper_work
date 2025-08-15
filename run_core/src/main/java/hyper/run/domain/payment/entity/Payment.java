package hyper.run.domain.payment.entity;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Table(name = "payment")
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id", updatable = false)
    private Long id;

    @Column(name = "price", nullable = false)
    private int price;

    @Column(name = "coupon_amount", nullable = true)
    private int couponAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private PaymentState state;

    @Column(name = "payment_method", nullable = true)
    private String paymentMethod;

    @Column(name = "payment_at", nullable = false)
    private LocalDateTime paymentAt;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    public void updateState(PaymentState updateState){
        this.state = updateState;
    }
}
