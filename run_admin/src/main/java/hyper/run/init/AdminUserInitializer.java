package hyper.run.init;

import hyper.run.domain.user.entity.LoginType;
import hyper.run.domain.user.entity.Role;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.initial.email}")
    private String adminEmail;

    @Value("${admin.initial.password}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User adminUser = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .name("관리자")
                    .phoneNumber("010-0000-0000")
                    .birth("1990-01-01")
                    .loginType(LoginType.EMAIL)
                    .role(Role.ADMIN)
                    .coupon(0)
                    .point(0.0)
                    .build();

            userRepository.save(adminUser);
            log.info("초기 관리자 계정이 생성되었습니다: {}", adminEmail);
        } else {
            log.info("관리자 계정이 이미 존재합니다: {}", adminEmail);
        }
    }
}
