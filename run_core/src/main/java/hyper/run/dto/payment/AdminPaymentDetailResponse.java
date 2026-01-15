package hyper.run.dto.payment;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.payment.entity.InAppType;
import hyper.run.domain.payment.entity.Payment;
import hyper.run.domain.payment.entity.PaymentState;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class AdminPaymentDetailResponse {
    private Long id;
    private int price;
    private int couponAmount;
    private InAppType inAppType;
    private PaymentState state;
    private String stateName;
    private String paymentMethod;
    private String transactionId;
    private String productId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdAtFormatted;
    private String updatedAtFormatted;

    // 사용자 정보
    private Long userId;
    private String userEmail;
    private String userName;
    private String userPhoneNumber;

    // 환불 요청 정보 (환불 요청 상태일 때만)
    private RefundInfo refundInfo;

    @Getter
    @Builder
    public static class RefundInfo {
        private Long inquiryId;
        private String refundReason; // message
        private String bankName;
        private String accountNumber;
        private Integer refundPrice;
    }

    public static AdminPaymentDetailResponse from(Payment payment, CustomerInquiry refundInquiry) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String createdAtFormatted = payment.getCreateDateTime() != null
                ? payment.getCreateDateTime().format(formatter)
                : "-";
        String updatedAtFormatted = payment.getModifiedDateTime() != null
                ? payment.getModifiedDateTime().format(formatter)
                : "-";

        // 환불 요청이 있으면 "환불요청", 없으면 Payment의 state 사용
        String stateName = refundInquiry != null ? "환불요청" : getStateNameInKorean(payment.getState());

        RefundInfo refundInfo = null;
        if (refundInquiry != null) {
            refundInfo = RefundInfo.builder()
                    .inquiryId(refundInquiry.getId())
                    .refundReason(refundInquiry.getMessage())
                    .bankName(refundInquiry.getBankName())
                    .accountNumber(refundInquiry.getAccountNumber())
                    .refundPrice(refundInquiry.getRefundPrice())
                    .build();
        }

        return AdminPaymentDetailResponse.builder()
                .id(payment.getId())
                .price(payment.getPrice())
                .couponAmount(payment.getCouponAmount())
                .inAppType(payment.getInAppType())
                .state(payment.getState())
                .stateName(stateName)
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .productId(payment.getProductId())
                .createdAt(payment.getCreateDateTime())
                .updatedAt(payment.getModifiedDateTime())
                .createdAtFormatted(createdAtFormatted)
                .updatedAtFormatted(updatedAtFormatted)
                .userId(payment.getUser() != null ? payment.getUser().getId() : null)
                .userEmail(payment.getUser() != null ? payment.getUser().getEmail() : "-")
                .userName(payment.getUser() != null ? payment.getUser().getName() : "-")
                .userPhoneNumber(payment.getUser() != null ? payment.getUser().getPhoneNumber() : "-")
                .refundInfo(refundInfo)
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
