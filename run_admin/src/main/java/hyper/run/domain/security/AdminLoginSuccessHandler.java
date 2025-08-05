package hyper.run.domain.security;

import hyper.run.domain.dto.response.AccessTokenPayload;
import hyper.run.domain.dto.response.RefreshTokenPayload;
import hyper.run.domain.entity.AdminUser;
import hyper.run.domain.repository.AdminUserRepository;
import hyper.run.domain.user.entity.Role;
import hyper.run.utils.OptionalUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AdminJwtService jwtService;
    private final AdminUserRepository adminUserRepository;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) {
        String email = extractUsername(authentication);
        AdminUser admin = OptionalUtil.getOrElseThrow(adminUserRepository.findByEmail(email),"존재하지 않는 관리자입니다.");

        String accessToken = jwtService.createAccessToken(new AccessTokenPayload(email, Role.ADMIN,new Date()));
        String refreshToken = jwtService.createRefreshToken(new RefreshTokenPayload(email,new Date()));
        jwtService.sendAccessAndRefreshToken(response,accessToken,refreshToken);

        admin.setRefreshToken(refreshToken);
        log.info("로그인에 성공한 관리자 정보 : " + email);
    }
    private String extractUsername(Authentication authentication){
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
}
