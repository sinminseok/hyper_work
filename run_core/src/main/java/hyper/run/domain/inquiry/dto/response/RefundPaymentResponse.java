package hyper.run.domain.inquiry.dto.response;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.entity.RefundType;
import hyper.run.domain.payment.entity.Payment;
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
    private LocalDateTime createDateTime;
    private Integer price;
    private RefundType refundType;
    private String message;

    public static RefundPaymentResponse paymentToRefundDto(Payment payment, CustomerInquiry inquiry){
        return RefundPaymentResponse.builder()
                .userName(payment.getUser().getName())
                .phoneNumber(payment.getUser().getPhoneNumber())
                .email(payment.getUser().getEmail())
                .paymentMethod(payment.getPaymentMethod())
                .createDateTime(payment.getCreateDateTime())
                .price(inquiry.getRefundPrice())
                .refundType(inquiry.getRefundType())
                .message(inquiry.getMessage())
                .build();
    }
}
