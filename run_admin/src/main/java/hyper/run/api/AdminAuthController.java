package hyper.run.api;


import hyper.run.domain.dto.response.LoginResponse;
import hyper.run.domain.service.AdminLoginService;

import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/admin/auth")
public class AdminAuthController {

    private AdminLoginService loginService;

    private ResponseEntity<?> createLoginResponse(final String email){
        LoginResponse loginResponse = loginService.login(email);
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION,loginResponse.getAccessToken())
                .header(HttpHeaders.SET_COOKIE,loginResponse.getRefreshTokenCookie().toString())
                .body(new SuccessResponse(true,"로그인 성공",loginResponse));

    }
}
