package hyper.run.domain.security;

import hyper.run.domain.dto.response.AccessTokenPayload;
import hyper.run.domain.entity.AdminUser;
import hyper.run.domain.repository.AdminUserRepository;
import hyper.run.domain.dto.response.LoginResponse;
import hyper.run.domain.service.AdminLoginService;
import hyper.run.utils.OptionalUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String NO_CHECK_URL = "/v1/api/admin/auth";

    private final AdminJwtService jwtService;
    private final AdminUserRepository adminUserRepository;
    private final CookieService cookieService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().equals(NO_CHECK_URL)) {
            filterChain.doFilter(request, response);
            return;
        }
        Optional<String> accessTokenOpt = jwtService.extractAccessToken(request);

        if (accessTokenOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        String accessToken = accessTokenOpt.get();

        try {
            jwtService.verifyToken(accessToken);
            setAuthentication(accessToken);
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            log.info("AccessToken 이 만료되었습니다. RefreshToken 재발급 시도");
            try {
                refreshTokensAndContinue(request, response, filterChain);
            } catch (JwtException | IOException | ServletException refreshException) {
                log.warn("RefreshToken 재발급 실패: {}", refreshException.getMessage());
                handleAuthError(response, "로그인 재시도 필요");
            }
        }
    }

    private void refreshTokensAndContinue(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        String refreshToken = cookieService.getRefreshTokenFromCookie(request)
                .orElseThrow(() -> new JwtException("존재하지 않는 cookie"));
        jwtService.verifyToken(refreshToken);

        AdminUser admin = OptionalUtil.getOrElseThrow(adminUserRepository.findByRefreshToken(refreshToken),"존재하지 않는 관리자입니다.");

        String newAccessToken = jwtService.createAccessToken(
                new AccessTokenPayload(admin.getEmail(), admin.getRole(), new Date())
        );

        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);
        setAuthentication(newAccessToken);
        filterChain.doFilter(request, response);
    }


    private void setAuthentication(String accessToken) {
        Claims claims = jwtService.verifyToken(accessToken);
        String email = claims.getSubject();
        String role = claims.get("role", String.class);

        // AdminUser 조회하여 userId 포함한 CustomAdminUserDetails 생성
        AdminUser adminUser = OptionalUtil.getOrElseThrow(
                adminUserRepository.findByEmail(email),
                "존재하지 않는 관리자입니다."
        );

        CustomAdminUserDetails userDetails = new CustomAdminUserDetails(
                adminUser.getId(),
                adminUser.getEmail(),
                role
        );

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void handleAuthError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"error\": \"%s\"}", message));
    }
}
