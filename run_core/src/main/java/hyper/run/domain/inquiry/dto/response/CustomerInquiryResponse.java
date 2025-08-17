package hyper.run.domain.inquiry.dto.response;

import hyper.run.domain.inquiry.entity.InquiryState;
import hyper.run.domain.inquiry.entity.InquiryType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class CustomerInquiryResponse {
    private LocalDateTime inquiredAt;
    private InquiryState state;
    private InquiryType type;
    private String userName;
    private String email;
    private String phoneNumber;
    private String title;
    private String message;


}
