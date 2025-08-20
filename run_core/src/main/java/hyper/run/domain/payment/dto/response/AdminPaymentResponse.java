package hyper.run.domain.payment.dto.response;

import hyper.run.domain.game.entity.Game;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import hyper.run.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminPaymentResponse {

    private Long paymentId;
    private LocalDateTime createDateTime;
    private String paymentMethod;
    private int price;
    private String name;
    private PaymentState state;

    public static AdminPaymentResponse paymentToAdminPayment(Payment payment){
        return AdminPaymentResponse.builder()
                .paymentId(payment.getId())
                .createDateTime(payment.getCreateDateTime())
                .paymentMethod(payment.getPaymentMethod())
                .price(payment.getPrice())
                .name(payment.getUser().getName())
                .state(payment.getState())
                .build();
    }

}
