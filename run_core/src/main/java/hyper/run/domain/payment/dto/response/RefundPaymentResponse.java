package hyper.run.domain.payment.dto.response;

import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class RefundPaymentResponse {
    private String userName;
    private String phoneNumber;
    private String email;
    private String paymentMethod;
    private LocalDateTime paymentAt;
    private int price;

    public static RefundPaymentResponse paymentToRefundDto(Payment payment){
        return RefundPaymentResponse.builder()
                .userName(payment.getUser().getName())
                .phoneNumber(payment.getUser().getPhoneNumber())
                .email(payment.getUser().getEmail())
                .paymentMethod(payment.getPaymentMethod())
                .paymentAt(payment.getPaymentAt())
                .price(payment.getPrice())
                .build();
    }
}
