package hyper.run.domain.user.dto.response;

import hyper.run.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserWatchConnectedResponse {
    private String watchConnectedKey;

    public static UserWatchConnectedResponse from(User user){
        return UserWatchConnectedResponse.builder()
                .watchConnectedKey(user.getWatchConnectedKey())
                .build();
    }
}
