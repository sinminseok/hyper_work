package hyper.run.domain.inquiry.event;

import hyper.run.domain.inquiry.entity.RefundType;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class RefundInquiryCreatedEvent {

    private final Long userId;
    private final String email;
    private final Long paymentId;
    private final Integer refundPrice;
    private final RefundType refundType;
    private final String accountNumber;
    private final String bankName;
    private final String title;
    private final String message;

    public static RefundInquiryCreatedEvent of(
            Long userId,
            String email,
            Long paymentId,
            Integer refundPrice,
            RefundType refundType,
            String accountNumber,
            String bankName,
            String title,
            String message
    ) {
        return new RefundInquiryCreatedEvent(
                userId,
                email,
                paymentId,
                refundPrice,
                refundType,
                accountNumber,
                bankName,
                title,
                message
        );
    }
}
