package hyper.run.domain.payment.dto.response;

import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PaymentResponse {

    private Long id;

    private int price;

    private int couponAmount;

    private PaymentState state;

    private String paymentMethod;

    private LocalDateTime paymentAt;

    public static PaymentResponse toResponse(Payment payment){
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentAt(payment.getPaymentAt())
                .couponAmount(payment.getCouponAmount())
                .price(payment.getPrice())
                .state(payment.getState())
                .paymentMethod(payment.getPaymentMethod())
                .build();
    }
}
