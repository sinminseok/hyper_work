package hyper.run.domain.payment.dto.request;

import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import hyper.run.domain.user.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PaymentRequest {

    private int price;

    private int couponAmount;

    private String paymentMethod;

    public Payment toEntity(User user){
        return Payment.builder()
                .price(this.price)
                .couponAmount(this.couponAmount)
                .state(PaymentState.PAYMENT_COMPLETED)
                .paymentMethod(this.paymentMethod)
                .paymentAt(LocalDateTime.now())
                .user(user)
                .build();
    }
}
