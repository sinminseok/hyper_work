package hyper.run.domain.inquiry.dto.request;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import hyper.run.domain.inquiry.entity.RefundType;
import hyper.run.domain.user.entity.User;
import lombok.Getter;

@Getter
public class InquiryRequest {
    private String email;

    private InquiryType type;

    private Long paymentId;

    private Integer refundPrice;

    private RefundType refundType;

    private String message;

    private String accountNumber;

    private String bankName;

    private String title;

    public CustomerInquiry toRefundInquiry(final User user){
        return CustomerInquiry.builder()
                .email(this.email)
                .user(user)
                .title(this.title)
                .paymentId(this.paymentId)
                .type(InquiryType.REFUND)
                .state(InquiryState.WAITING)
                .refundPrice(this.refundPrice)
                .refundType(this.refundType)
                .accountNumber(this.accountNumber)
                .bankName(this.bankName)
                .message(this.message)
                .build();
    }

    public CustomerInquiry toCommonInquiry(final User user){
        return CustomerInquiry.builder()
                .user(user)
                .email(this.email)
                .state(InquiryState.WAITING)
                .type(this.type)
                .message(this.message)
                .build();
    }
}
