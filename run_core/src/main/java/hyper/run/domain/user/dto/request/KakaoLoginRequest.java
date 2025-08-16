package hyper.run.domain.user.dto.request;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoLoginRequest {
    private String accessToken;

    public KakaoLoginRequest(String accessToken) {
        this.accessToken = accessToken;
    }
}