package hyper.run.domain.user.dto.request;


import hyper.run.domain.user.entity.LoginType;
import hyper.run.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSignupRequest {

    private String name;

    private String email;

    private String password;

    private String birth;

    private String phoneNumber;

    private LoginType loginType;

    public User toEntity(final String encodePassword){
        return User.builder()
                .name(this.name)
                .birth(this.birth)
                .email(this.email)
                .coupon(0)
                .point(0)
                .phoneNumber(this.phoneNumber)
                .loginType(this.loginType)
                .password(encodePassword)
                .build();
    }
}