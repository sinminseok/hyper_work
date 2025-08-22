package hyper.run.helper;

import hyper.run.domain.user.entity.LoginType;
import hyper.run.domain.user.entity.User;

public class UserHelper {

    public static User generateUser(Long id){
        return User.builder()
                .name("사용자" + id)
                .email("testEmail"+id)
                .password("password")
                .phoneNumber("0101234123"+id)
                .birth("001031")
                .loginType(LoginType.EMAIL)
                .coupon(100)
                .point(0)
                .build();
    }
}
