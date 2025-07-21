package hyper.run.domain.user.entity;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.game.entity.GameHistory;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.exception.custom.InsufficientCouponException;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

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

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = true)
    private String email;

    @Setter
    @Column(name = "password", nullable = true)
    private String password;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "brith", nullable = false)
    private String brith;

    @Column(name = "login_type", nullable = false)
    private LoginType loginType;

    @Column(name = "coupon", nullable = false)
    private int coupon;

    @Column(name = "profile_url", nullable = true)
    private String profileUrl;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Payment> payments;

    @Setter
    @Column(name = "refreshToken", nullable = true)
    private String refreshToken;


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
            throw new InsufficientCouponException("쿠폰 수량 부족");
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
