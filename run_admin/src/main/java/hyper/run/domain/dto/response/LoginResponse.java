package hyper.run.domain.dto.response;

import hyper.run.domain.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseCookie;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponse {

    private Role role;
    private String accessToken;
    private ResponseCookie refreshTokenCookie;

}
