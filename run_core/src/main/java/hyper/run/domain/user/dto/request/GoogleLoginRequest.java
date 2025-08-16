package hyper.run.domain.user.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GoogleLoginRequest {
    private String idToken;
    private PlatformType platformType;
}
