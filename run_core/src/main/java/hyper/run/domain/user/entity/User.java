package hyper.run.domain.user.entity;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.exception.custom.InsufficientCouponException;
import hyper.run.exception.custom.NotEnoughRefundAmount;
import hyper.run.exception.custom.UserDuplicatedException;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import static hyper.run.exception.ErrorMessages.NOT_ENOUGH_COUPON_AMOUNT;
import static hyper.run.exception.ErrorMessages.NOT_ENOUGH_TO_REFUND_AMOUNT;

@Table(name = "user")
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false)
    private Long id;

    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = true)
    private String email;

    @Setter
    @Column(name = "password", nullable = true)
    private String password;

    @Setter
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Setter
    @Column(name = "brith", nullable = false)
    private String brith;

    @Column(name = "login_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LoginType loginType;

    @Column(name = "coupon", nullable = false)
    private int coupon;

    @Setter
    @Column(name = "point", nullable = false)
    private double point;

    @Column(name = "profile_url", nullable = true)
    private String profileUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments;

    @Setter
    @Column(name = "refreshToken", nullable = true)
    private String refreshToken;

    @Setter
    @Column(name = "watch_connected_key", nullable = true)
    private String watchConnectedKey;

    @Setter
    @Column(name = "accessToken", nullable = true)
    private String accessToken;

    public void validateRefundPossible(final int couponCount){
        if(this.coupon < couponCount){
            throw new NotEnoughRefundAmount(NOT_ENOUGH_TO_REFUND_AMOUNT);
        }
    }

    public void validateExchange(final double amount){
        if(this.point < amount){
            throw new NotEnoughRefundAmount(NOT_ENOUGH_TO_REFUND_AMOUNT);
        }
    }

    public void decreasePoint(double amount){
        this.point -= amount;
    }

    public void increasePoint(double updatePoint){
        this.point += updatePoint;
    }

    public void decreaseCoupon(){
        this.coupon -= 1;
    }

    public void increaseCoupon(){
        this.coupon += 1;
    }

    public void chargeCoupon(final int amount){
        this.coupon += amount;
    }

    public void validateCouponAmount(){
        if(coupon < 1) {
            throw new InsufficientCouponException(NOT_ENOUGH_COUPON_AMOUNT);
        }
    }

    public void addPayment(Payment payment){
        this.payments.add(payment);
        payment.setUser(this); // 양방향 관계 유지
    }
    public void updatePassword(final String encodePassword){
        this.password = encodePassword;
    }
}
