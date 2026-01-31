package hyper.run.dto.payment;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPaymentListResponse {
    private Long id;
    private LocalDateTime createdAt;
    private String createdAtFormatted;
    private int price;
    private String userEmail;
    private PaymentState state;
    private String stateName;
    private boolean hasRefundRequest; // 환불 요청 여부

    public static AdminPaymentListResponse from(Payment payment, CustomerInquiry refundInquiry) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String createdAtFormatted = payment.getCreateDateTime() != null
                ? payment.getCreateDateTime().format(formatter)
                : "-";

        // 환불 요청이 있으면 "환불요청", 없으면 Payment의 state 사용
        String stateName;
        boolean hasRefundRequest = refundInquiry != null;

        if (hasRefundRequest) {
            stateName = "환불요청";
        } else {
            stateName = getStateNameInKorean(payment.getState());
        }

        return AdminPaymentListResponse.builder()
                .id(payment.getId())
                .createdAt(payment.getCreateDateTime())
                .createdAtFormatted(createdAtFormatted)
                .price(payment.getPrice())
                .userEmail(payment.getUser() != null ? payment.getUser().getEmail() : "-")
                .state(payment.getState())
                .stateName(stateName)
                .hasRefundRequest(hasRefundRequest)
                .build();
    }

    private static String getStateNameInKorean(PaymentState state) {
        return switch (state) {
            case PENDING -> "대기중";
            case PAYMENT_COMPLETED -> "결제완료";
            case REFUND_REQUESTED -> "환불요청";
            case REFUND_REJECTED -> "환불거절";
            case REFUND_COMPLETED -> "환불완료";
        };
    }
}
