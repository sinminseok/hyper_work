package hyper.run.domain.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CookieService {

    @Value("${jwt.refresh.expiration}")
    private long refreshKeyExpiration;

    private final String REFRESH_TOKEN = "refreshToken";

    public Optional<String> getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) {
            return Optional.empty();
        }

        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(REFRESH_TOKEN)) {
                return Optional.ofNullable(cookie.getValue());
            }
        }
        return Optional.empty();
    }


    public ResponseCookie createRefreshTokenCookie(final String refreshToken) {
        return ResponseCookie.from("refreshToken",refreshToken)
                .httpOnly(true)
                .secure(true) // HTTPS 환경에서만 하는거면 true
                .maxAge(refreshKeyExpiration)
                .sameSite("None") // Cross-site 쿠키허용(도메인 다를경우) - none, 개발환경 : Strict
                .path("/")
                .build();
    }

}
