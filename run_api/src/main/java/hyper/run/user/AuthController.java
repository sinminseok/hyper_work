package hyper.run.user;

import hyper.run.auth.domain.CustomUserDetails;
import hyper.run.auth.dto.LoginResponse;
import hyper.run.auth.service.TokenCustomService;
import hyper.run.domain.user.entity.User;
import hyper.run.domain.user.repository.UserRepository;
import hyper.run.domain.user.service.AuthCodeService;
import hyper.run.domain.user.service.SmsService;
import hyper.run.exception.ErrorResponseCode;
import hyper.run.exception.custom.AuthException;
import hyper.run.utils.AuthCodeUtil;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCodeService authCodeService;
    private final SmsService smsService;
    private final UserRepository userRepository;
    private final TokenCustomService tokenCustomService;

    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestParam String phoneNumber) {
        String code = AuthCodeUtil.generate6DigitCode();
        authCodeService.saveAuthCode(phoneNumber, code);
        smsService.sendSms(phoneNumber, code);
        SuccessResponse response = new SuccessResponse(true, "인증번호 전송 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestParam String phoneNumber,
                                        @RequestParam String code) {
        boolean result = authCodeService.verifyCode(phoneNumber, code);
        if (result) {
            SuccessResponse response = new SuccessResponse(true, "인증성공", null);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        SuccessResponse response = new SuccessResponse(false, "인증실패", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/logout")
    @Transactional
    public ResponseEntity<?> logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthException(ErrorResponseCode.NOT_VALID_TOKEN, "인증되지 않은 사용자입니다.");
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long userId = userDetails.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(ErrorResponseCode.NOT_VALID_TOKEN, "사용자를 찾을 수 없습니다."));

        // RefreshToken 삭제 (로그아웃 처리)
        user.setRefreshToken(null);

        SuccessResponse response = new SuccessResponse(true, "로그아웃 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization-refresh") String refreshToken) {
        String token = refreshToken.replace("Bearer ", "");
        LoginResponse loginResponse = tokenCustomService.processRefreshToken(token);
        SuccessResponse response = new SuccessResponse(true, "토큰 재발급 성공", loginResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}