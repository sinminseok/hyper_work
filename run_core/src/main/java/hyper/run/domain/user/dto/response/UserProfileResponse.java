package hyper.run.domain.user.dto.response;

import hyper.run.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {

    private Long id;

    private String name;

    private String brith;

    private String phoneNumber;

    private String profileUrl;

    public static UserProfileResponse toProfileResponse(User user){
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .brith(user.getBrith())
                .phoneNumber(user.getPhoneNumber())
                .profileUrl(user.getProfileUrl())
                .build();
    }
}
