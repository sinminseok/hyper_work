package hyper.run.domain.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hyper.run.domain.dto.response.AccessTokenPayload;
import hyper.run.domain.dto.response.LoginResponse;
import hyper.run.domain.dto.response.RefreshTokenPayload;
import hyper.run.domain.entity.AdminUser;
import hyper.run.domain.repository.AdminUserRepository;
import hyper.run.domain.service.AdminLoginService;
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

import java.io.IOException;
import java.util.Date;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AdminLoginService adminLoginService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String email = extractUsername(authentication);
        LoginResponse loginResponse = adminLoginService.login(email);
        sendLoginSuccessResponse(response, loginResponse);

        log.info("로그인에 성공한 관리자 정보 : " + email);
    }

    private void sendLoginSuccessResponse(HttpServletResponse response, LoginResponse loginResponse) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.addHeader("Set-Cookie", loginResponse.getRefreshTokenCookie().toString());

        objectMapper.writeValue(response.getWriter(),
                Map.of("role", loginResponse.getRole(), "accessToken", loginResponse.getAccessToken()));
    }

    private String extractUsername(Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }

}
