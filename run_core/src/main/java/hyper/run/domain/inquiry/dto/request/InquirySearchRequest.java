package hyper.run.domain.inquiry.dto.request;

import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Setter
public class InquirySearchRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private InquiryState state;
    private InquiryType type;

}
