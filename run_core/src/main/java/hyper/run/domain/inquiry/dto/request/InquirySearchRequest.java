package hyper.run.domain.inquiry.dto.request;

import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class InquirySearchRequest {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String keyword;
    private InquiryState state;
    private InquiryType type;

}
