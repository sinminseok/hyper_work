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
public class UserProfileResponse {

    private Long id;

    private String name;

    private String birth;

    private String phoneNumber;

    private String profileUrl;

    private double point;

    public static UserProfileResponse toProfileResponse(User user){
        return UserProfileResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .birth(user.getBirth())
                .phoneNumber(user.getPhoneNumber())
                .profileUrl(user.getProfileUrl())
                .point(user.getPoint())
                .build();
    }
}
