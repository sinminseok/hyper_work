package hyper.run.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LoginResponse{
    String accessToken;
    String refreshToken;

}
