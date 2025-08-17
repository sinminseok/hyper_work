package hyper.run.domain.inquiry.dto.request;

import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class InquirySearchRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private InquiryState state;
    private InquiryType type;

}
