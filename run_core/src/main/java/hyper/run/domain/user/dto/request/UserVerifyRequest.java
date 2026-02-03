package hyper.run.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 본인 확인 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserVerifyRequest {
    private String email;
    private String password;
}
