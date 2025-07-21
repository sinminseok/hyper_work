package hyper.run.domain.user.dto.request;


import hyper.run.domain.user.entity.LoginType;
import hyper.run.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserSignupRequest {

    private String name;

    private String email;

    private String password;

    private String  brith;

    private String phoneNumber;

    private LoginType loginType;

    public User toEntity(final String encodePassword){
        return User.builder()
                .name(this.name)
                .brith(this.brith)
                .email(this.email)
                .coupon(0)
                .point(0)
                .phoneNumber(this.phoneNumber)
                .loginType(this.loginType)
                .password(encodePassword)
                .build();
    }
}