package hyper.run.domain.inquiry.dto.request;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import hyper.run.domain.inquiry.entity.RefundType;
import lombok.Getter;

@Getter
public class InquiryRequest {

    private String email;

    private InquiryType type;

    private Long paymentId;

    private Integer refundPrice;

    private RefundType refundType;

    private String message;

    public CustomerInquiry toRefundInquiry(final Long userId){
        return CustomerInquiry.builder()
                .email(this.email)
                .userId(userId)
                .type(InquiryType.REFUND)
                .state(InquiryState.WAITIN)
                .refundPrice(this.refundPrice)
                .refundType(this.refundType)
                .message(this.message)
                .build();
    }

    public CustomerInquiry toCommonInquiry(final Long userId){
        return CustomerInquiry.builder()
                .email(this.email)
                .userId(userId)
                .state(InquiryState.WAITIN)
                .type(this.type)
                .message(this.message)
                .build();
    }
}
