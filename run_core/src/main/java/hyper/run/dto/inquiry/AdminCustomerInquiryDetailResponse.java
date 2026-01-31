package hyper.run.dto.inquiry;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import hyper.run.domain.inquiry.entity.RefundType;
import hyper.run.domain.user.entity.User;
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
public class AdminCustomerInquiryDetailResponse {
    private Long id;
    private Long userId;
    private Long paymentId;
    private String email;
    private String userName;
    private String phoneNumber;
    private InquiryType type;
    private String typeName;
    private Integer refundPrice;
    private RefundType refundType;
    private String refundTypeName;
    private InquiryState state;
    private String stateName;
    private String accountNumber;
    private String bankName;
    private String title;
    private String message;
    private String answer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdAtFormatted;
    private String updatedAtFormatted;

    public static AdminCustomerInquiryDetailResponse from(CustomerInquiry inquiry, User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String createdAtFormatted = inquiry.getCreateDateTime() != null
                ? inquiry.getCreateDateTime().format(formatter)
                : "-";
        String updatedAtFormatted = inquiry.getModifiedDateTime() != null
                ? inquiry.getModifiedDateTime().format(formatter)
                : "-";

        String stateName = getStateNameInKorean(inquiry.getState());
        String typeName = getTypeNameInKorean(inquiry.getType());
        String refundTypeName = inquiry.getRefundType() != null
                ? getRefundTypeNameInKorean(inquiry.getRefundType())
                : null;

        return AdminCustomerInquiryDetailResponse.builder()
                .id(inquiry.getId())
                .userId(user != null ? user.getId() : null)
                .paymentId(inquiry.getPaymentId())
                .email(inquiry.getEmail())
                .userName(user != null ? user.getName() : "-")
                .phoneNumber(user != null ? user.getPhoneNumber() : "-")
                .type(inquiry.getType())
                .typeName(typeName)
                .refundPrice(inquiry.getRefundPrice())
                .refundType(inquiry.getRefundType())
                .refundTypeName(refundTypeName)
                .state(inquiry.getState())
                .stateName(stateName)
                .accountNumber(inquiry.getAccountNumber())
                .bankName(inquiry.getBankName())
                .title(inquiry.getTitle())
                .message(inquiry.getMessage())
                .answer(inquiry.getAnswer())
                .createdAt(inquiry.getCreateDateTime())
                .updatedAt(inquiry.getModifiedDateTime())
                .createdAtFormatted(createdAtFormatted)
                .updatedAtFormatted(updatedAtFormatted)
                .build();
    }

    private static String getStateNameInKorean(InquiryState state) {
        return switch (state) {
            case SUCCESS -> "답변 완료";
            case WAITING -> "문의중";
        };
    }

    private static String getTypeNameInKorean(InquiryType type) {
        return switch (type) {
            case REFUND -> "환불";
            case ACCOUNT -> "계정";
            case PAYMENT -> "결제";
            case GAME -> "경기";
            case APP -> "앱 이용";
            case USER -> "회원신고";
            case OTHER -> "기타";
        };
    }

    private static String getRefundTypeNameInKorean(RefundType refundType) {
        return switch (refundType) {
            case LACK_OF_FEATURES -> "기능 부족";
            case COMPLICATED_INTERFACE -> "복잡한 인터페이스";
            case FREQUENT_ERRORS -> "잦은 오류 발생";
            case TOO_EXPENSIVE -> "가격 부담";
            case MISTAKEN_PAYMENT -> "오인 결제";
            case POOR_CUSTOMER_SUPPORT -> "고객 지원 불만족";
            case OTHER -> "기타";
        };
    }
}
