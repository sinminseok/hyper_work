package hyper.run.domain.inquiry.dto.request;

import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


@AllArgsConstructor
@Getter
@Builder
public class InquirySearchRequest {
    private final LocalDateTime startDate;
    private final LocalDateTime endDate;
    private final String keyword;
    private final InquiryState state;
    private final InquiryType type;
}
