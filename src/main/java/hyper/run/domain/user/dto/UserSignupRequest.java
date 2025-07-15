package hyper.run.domain.user.dto;


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
                .phoneNumber(this.phoneNumber)
                .password(encodePassword)
                .build();
    }
}