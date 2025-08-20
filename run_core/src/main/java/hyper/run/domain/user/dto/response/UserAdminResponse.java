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
public class UserAdminResponse {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private String birth;
//    private boolean usageAgreement;

    public static UserAdminResponse userToAdminUserDto(User user){
        return UserAdminResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .birth(user.getBirth())
                .build();
    }
}
