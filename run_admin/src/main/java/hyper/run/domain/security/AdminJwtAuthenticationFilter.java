package hyper.run.domain.security;

import hyper.run.domain.entity.AdminUser;
import hyper.run.domain.repository.AdminUserRepository;
import hyper.run.domain.dto.response.LoginResponse;
import hyper.run.domain.service.AdminLoginService;
import hyper.run.utils.OptionalUtil;
import io.jsonwebtoken.Claims;
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
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String NO_CHECK_URL = "/login";

    private final AdminJwtService jwtService;
    private final AdminUserRepository adminUserRepository;
    private final CookieService cookieService;
    private final AdminLoginService loginService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if(request.getRequestURI().equals("/v1/api/admin/login")){
            filterChain.doFilter(request,response);
            return;
        }
        Optional<String> refreshToken = cookieService.getRefreshTokenFromCookie(request);
        Optional<String> accessToken = jwtService.extractAccessToken(request);
        try{
            // 리프레시 토큰 존재 확인 후 재발급
            if(refreshToken.isPresent()){
                reissueTokens(request,response);
                return;
            }
            // 리프레시 토큰 없지만 엑세스 토큰 존재하면 유효성 검사
            else if (accessToken.isPresent()){
                validateAccessToken(accessToken.get());
            }
        }catch (JwtException e){
            log.warn("유효하지 않은 토큰입니다. : {} ",request.getRequestURI());
        }

        filterChain.doFilter(request,response);
    }

    // SecuirtyContext 에 저장
    private void validateAccessToken(String accessToken){
        Claims claims = jwtService.verifyToken(accessToken);
        String email = claims.getSubject();
        String role = claims.get("role",String.class);
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(role);
        Authentication authentication = new UsernamePasswordAuthenticationToken(email,null,List.of(grantedAuthority));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    // 토큰 재발급 후 응답 처리
    private void reissueTokens(HttpServletRequest request,HttpServletResponse response) throws IOException{
        String refreshToken = cookieService.getRefreshTokenFromCookie(request).orElseThrow(() -> new JwtException("리프레시 토큰 없습니다"));

        jwtService.verifyToken(refreshToken);

        AdminUser admin = OptionalUtil.getOrElseThrow(adminUserRepository.findByRefreshToken(refreshToken),"존재하지 않는 사용자입니다.");

        LoginResponse loginResponse = loginService.login(admin.getEmail());
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + loginResponse.getAccessToken());
        response.addHeader(HttpHeaders.SET_COOKIE, loginResponse.getRefreshTokenCookie().toString());
        response.setStatus(HttpServletResponse.SC_OK);

    }
}
