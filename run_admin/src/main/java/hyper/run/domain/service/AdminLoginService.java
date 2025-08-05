package hyper.run.domain.service;

import hyper.run.domain.dto.response.AccessTokenPayload;
import hyper.run.domain.dto.response.LoginResponse;
import hyper.run.domain.dto.response.RefreshTokenPayload;
import hyper.run.domain.entity.AdminUser;

import hyper.run.domain.entity.RefreshToken;
import hyper.run.domain.repository.AdminUserRepository;
import hyper.run.domain.repository.RefreshTokenRepository;
import hyper.run.domain.security.AdminJwtService;
import hyper.run.domain.security.CookieService;
import hyper.run.utils.OptionalUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AdminLoginService {

    @Value("${jwt.access.expiration}")
    private long accessKeyExpiration; // 30분

    @Value("${jwt.refresh.expiration}")
    private long refreshKeyExpiration; // 14일

    private final AdminUserRepository adminUserRepository;
    private final CookieService cookieService;
    private final AdminJwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public LoginResponse login(final String email){
        AdminUser admin = OptionalUtil.getOrElseThrow(adminUserRepository.findByEmail(email),"존재하지 않는 관리자 이메일입니다.");

        String refreshToken = jwtService.createRefreshToken(new RefreshTokenPayload(admin.getEmail(), new Date()));
        updateRefreshToken(admin,refreshToken);

        String accessToken = jwtService.createAccessToken(new AccessTokenPayload(admin.getEmail(),admin.getRole(),new Date()));
        ResponseCookie responseCookie = cookieService.createRefreshTokenCookie(refreshToken);
        return new LoginResponse(admin.getRole(),accessToken,responseCookie);

    }

    public void updateRefreshToken(AdminUser admin,String token){
        refreshTokenRepository.findByAdmin(admin).ifPresent(refreshTokenRepository::delete);
        refreshTokenRepository.save(RefreshToken.builder()
                .token(token)
                .createdAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plus(Duration.ofMillis(refreshKeyExpiration)))
                .build());
    }
}
