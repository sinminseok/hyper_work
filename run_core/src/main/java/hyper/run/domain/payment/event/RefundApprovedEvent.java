package hyper.run.domain.payment.event;

import lombok.Getter;


@Getter
public class RefundApprovedEvent {
    private final Long paymentId;
    private final Long inquiryId;
    private final Long userId;
    private final String reason;

    // 이메일 전송용 정보
    private final String userName;
    private final String userEmail;
    private final Integer refundPrice;
    private final String bankName;
    private final String accountNumber;

    public RefundApprovedEvent(
            Long paymentId,
            Long inquiryId,
            Long userId,
            String reason,
            String userName,
            String userEmail,
            Integer refundPrice,
            String bankName,
            String accountNumber
    ) {
        this.paymentId = paymentId;
        this.inquiryId = inquiryId;
        this.userId = userId;
        this.reason = reason;
        this.userName = userName;
        this.userEmail = userEmail;
        this.refundPrice = refundPrice;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
    }
}
