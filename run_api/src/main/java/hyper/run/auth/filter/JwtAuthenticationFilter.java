package hyper.run.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import hyper.run.auth.domain.CustomUserDetails;
import hyper.run.auth.service.JwtService;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.exception.ErrorResponseCode;
import hyper.run.exception.custom.AuthException;
import hyper.run.utils.SuccessResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String NO_CHECK_URL = "/login";

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().equals(NO_CHECK_URL)) {
            filterChain.doFilter(request, response);
            return;
        }

        // /refresh 엔드포인트는 인증 체크 제외
        if (request.getRequestURI().equals("/v1/api/auth/refresh")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            checkAccessTokenAndAuthentication(request, response, filterChain);
        } catch (Exception ex) {
            handleException(response, ex);
        }
    }
    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                  FilterChain filterChain) throws ServletException, IOException {

        jwtService.extractAccessToken(request)
                .ifPresent(accessToken -> {
                    try {
                        jwtService.isTokenValid(accessToken);

                        jwtService.extractEmail(accessToken)
                                .ifPresent(email -> {
                                    userRepository.findByEmail(email)
                                            .ifPresent(user -> {
                                                saveAuthentication(user);
                                            });
                                });
                    } catch (Exception e) {
                        log.error("Token validation failed: {}", e.getMessage());
                        throw new AuthException(ErrorResponseCode.NOT_VALID_TOKEN, "유효하지 않는 토큰입니다.");
                    }
                });


        filterChain.doFilter(request, response);
    }

    public void saveAuthentication(User user) {
        CustomUserDetails userDetailsUser = new CustomUserDetails(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );


        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetailsUser,
                null,
                userDetailsUser.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void handleException(HttpServletResponse response, Exception ex) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");

        SuccessResponse<?> errorResponse;
        if (ex instanceof AuthException) {
            AuthException authEx = (AuthException) ex;
            errorResponse = new SuccessResponse<>(false, authEx.getErrorMessage(), authEx.getErrorCode().getCode());
        } else {
            errorResponse = new SuccessResponse<>(false, ex.getMessage(), null);
        }

        response.getWriter().write(new ObjectMapper().writeValueAsString(errorResponse));
    }
}
