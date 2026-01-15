package hyper.run.domain.payment.event;

import lombok.Getter;

/**
 * 환불 거절 이벤트
 * 관리자가 환불을 거절했을 때 발행됩니다.
 */
@Getter
public class RefundRejectedEvent {
    private final Long paymentId;
    private final Long inquiryId;
    private final Long userId;
    private final String reason;
    private final Integer couponAmount;

    // 이메일 전송용 정보
    private final String userName;
    private final String userEmail;

    public RefundRejectedEvent(
            Long paymentId,
            Long inquiryId,
            Long userId,
            String reason,
            Integer couponAmount,
            String userName,
            String userEmail
    ) {
        this.paymentId = paymentId;
        this.inquiryId = inquiryId;
        this.userId = userId;
        this.reason = reason;
        this.couponAmount = couponAmount;
        this.userName = userName;
        this.userEmail = userEmail;
    }
}
