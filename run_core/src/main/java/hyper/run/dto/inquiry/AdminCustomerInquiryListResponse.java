package hyper.run.dto.inquiry;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
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
public class AdminCustomerInquiryListResponse {
    private Long id;
    private LocalDateTime createdAt;
    private String createdAtFormatted;
    private InquiryState state;
    private String stateName;
    private InquiryType type;
    private String typeName;
    private String userName;
    private String email;
    private String phoneNumber;

    public static AdminCustomerInquiryListResponse from(CustomerInquiry inquiry, User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String createdAtFormatted = inquiry.getCreateDateTime() != null
                ? inquiry.getCreateDateTime().format(formatter)
                : "-";

        String stateName = getStateNameInKorean(inquiry.getState());
        String typeName = getTypeNameInKorean(inquiry.getType());

        return AdminCustomerInquiryListResponse.builder()
                .id(inquiry.getId())
                .createdAt(inquiry.getCreateDateTime())
                .createdAtFormatted(createdAtFormatted)
                .state(inquiry.getState())
                .stateName(stateName)
                .type(inquiry.getType())
                .typeName(typeName)
                .userName(user != null ? user.getName() : "-")
                .email(inquiry.getEmail())
                .phoneNumber(user != null ? user.getPhoneNumber() : "-")
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
}
