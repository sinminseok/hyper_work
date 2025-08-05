package hyper.run.domain.payment.dto.response;

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

    private Long id;
    private LocalDateTime paymentAt;
    private String paymentMethod;
    private int price;
    private User user;
    private PaymentState paymentState;

    public static AdminPaymentResponse paymentToAdminPayment(Payment payment,User user){
        return AdminPaymentResponse.builder()
                .id(payment.getId())
                .paymentAt(payment.getPaymentAt())
                .paymentMethod(payment.getPaymentMethod())
                .price(payment.getPrice())
                .user(user)
                .paymentState(payment.getState())
                .build();
    }

}
