package hyper.run.user;

import com.nimbusds.jose.JOSEException;
import hyper.run.auth.dto.LoginResponse;
import hyper.run.auth.service.JwtService;
import hyper.run.domain.user.dto.request.AppleLoginRequest;
import hyper.run.domain.user.dto.request.GoogleLoginRequest;
import hyper.run.domain.user.dto.request.KakaoLoginRequest;
import hyper.run.domain.user.dto.request.NaverLoginRequest;
import hyper.run.domain.user.service.oauth.AppleAuthService;
import hyper.run.domain.user.service.oauth.GoogleAuthService;
import hyper.run.domain.user.service.oauth.KakaoAuthService;
import hyper.run.domain.user.service.UserService;
import hyper.run.domain.user.service.oauth.NaverAuthService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.ParseException;


@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/oauth")
public class Oauth2Controller {

    private final JwtService jwtService;
    private final KakaoAuthService kakaoAuthService;
    private final NaverAuthService naverAuthService;
    private final GoogleAuthService googleAuthService;
    private final AppleAuthService appleAuthService;
    private final UserService userService;

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody GoogleLoginRequest request) throws GeneralSecurityException, IOException {
        String googleEmail = googleAuthService.getGoogleEmail(request);
        if (!userService.isExistEmail(googleEmail)) {
            SuccessResponse successResponse = new SuccessResponse(false, "가입이 필요합니다.", googleEmail);
            return new ResponseEntity<>(successResponse, HttpStatus.OK);
        }
        SuccessResponse successResponse = new SuccessResponse(true, "카카오 로그인 성공", createLoginResponse(googleEmail));
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody KakaoLoginRequest request) {
        String kakaoEmail = kakaoAuthService.getKaKaoEmail(request.getAccessToken());
        if (!userService.isExistEmail(kakaoEmail)) {
            SuccessResponse successResponse = new SuccessResponse(false, "가입이 필요합니다.", kakaoEmail);
            return new ResponseEntity<>(successResponse, HttpStatus.OK);
        }
        SuccessResponse successResponse = new SuccessResponse(true, "카카오 로그인 성공", createLoginResponse(kakaoEmail));
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @PostMapping("/naver")
    public ResponseEntity<?> naverLogin(@RequestBody NaverLoginRequest request) {
        String email = naverAuthService.getNaverEmail(request.getAccessToken());
        if (!userService.isExistEmail(email)) {
            SuccessResponse successResponse = new SuccessResponse(false, "가입이 필요합니다.", email);
            return new ResponseEntity<>(successResponse, HttpStatus.OK);
        }
        SuccessResponse successResponse = new SuccessResponse(true, "네이버 로그인 성공", createLoginResponse(email));
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    @PostMapping("/apple")
    public ResponseEntity<?> appleLogin(@RequestBody AppleLoginRequest request) throws ParseException, IOException, JOSEException {
        String email = appleAuthService.getAppleEmail(request.getIdentityToken());
        if (!userService.isExistEmail(email)) {
            SuccessResponse successResponse = new SuccessResponse(false, "가입이 필요합니다.", email);
            return new ResponseEntity<>(successResponse, HttpStatus.OK);
        }
        SuccessResponse successResponse = new SuccessResponse(true, "네이버 로그인 성공", createLoginResponse(email));
        return new ResponseEntity<>(successResponse, HttpStatus.OK);
    }

    private LoginResponse createLoginResponse(final String email) {
        String accessToken = jwtService.createAccessToken(email);
        String refreshToken = jwtService.createRefreshToken();
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}