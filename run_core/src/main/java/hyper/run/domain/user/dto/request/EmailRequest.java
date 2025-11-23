package hyper.run.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailRequest {

    @NotBlank(message = "수신자 이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String to;

    @NotBlank(message = "제목은 필수입니다")
    private String subject;

    @NotBlank(message = "내용은 필수입니다")
    private String text;

    // 선택적 필드들
    private String from;  // 발신자 이메일 (설정하지 않으면 기본값 사용)

    private List<String> cc;  // 참조

    private List<String> bcc;  // 숨은 참조

    private Boolean isHtml;  // HTML 여부 (기본값: false)
}