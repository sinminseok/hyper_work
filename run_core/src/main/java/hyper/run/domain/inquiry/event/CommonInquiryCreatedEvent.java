package hyper.run.domain.inquiry.event;

import hyper.run.domain.inquiry.entity.InquiryType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 일반 문의 생성 이벤트
 * - 사용자가 일반 문의를 신청할 때 발행 (환불 제외)
 * - Inquiry 저장만 리스너에서 처리
 */
@Getter
@AllArgsConstructor
public class CommonInquiryCreatedEvent {

    private final Long userId;
    private final String email;
    private final InquiryType inquiryType;
    private final String title;
    private final String message;

    public static CommonInquiryCreatedEvent of(
            Long userId,
            String email,
            InquiryType inquiryType,
            String title,
            String message
    ) {
        return new CommonInquiryCreatedEvent(
                userId,
                email,
                inquiryType,
                title,
                message
        );
    }
}
