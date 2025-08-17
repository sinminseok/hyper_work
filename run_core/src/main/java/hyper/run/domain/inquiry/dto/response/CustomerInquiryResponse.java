package hyper.run.domain.inquiry.dto.response;

import hyper.run.domain.inquiry.entity.CustomerInquiry;
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
public class CustomerInquiryResponse {
    private LocalDate inquiredAt;
    private InquiryState state;
    private InquiryType type;
    private String userName;
    private String email;
    private String phoneNumber;
    private String title;
    private String message;
    private String answer;

}
