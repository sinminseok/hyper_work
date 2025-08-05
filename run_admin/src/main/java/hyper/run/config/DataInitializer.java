package hyper.run.config;

import hyper.run.domain.dto.response.RefreshTokenPayload;
import hyper.run.domain.entity.AdminUser;
import hyper.run.domain.repository.AdminUserRepository;
import hyper.run.domain.security.AdminJwtService;
import hyper.run.domain.user.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminJwtService jwtService;

    @Override
    public void run(String... args) throws Exception {

        String adminEmail = "admin@naver.com";

        RefreshTokenPayload refreshTokenPayload = new RefreshTokenPayload(adminEmail,new Date());
        String refreshToken = jwtService.createRefreshToken(refreshTokenPayload);

        if(!adminUserRepository.findByEmail(adminEmail).isPresent()){
            AdminUser adminUser = AdminUser.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode("admin1234"))
                    .role(Role.ADMIN)
                    .refreshToken(refreshToken)
                    .build();
            adminUserRepository.save(adminUser);
        }
    }
}
