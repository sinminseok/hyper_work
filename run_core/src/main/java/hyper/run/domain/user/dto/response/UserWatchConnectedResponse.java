package hyper.run.domain.user.dto.response;

import hyper.run.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserWatchConnectedResponse {
    private String accessToken;
    private String watchConnectedKey;

    public static UserWatchConnectedResponse from(User user){
        return UserWatchConnectedResponse.builder()
                .accessToken(user.getAccessToken())
                .watchConnectedKey(user.getWatchConnectedKey())
                .build();
    }
}
