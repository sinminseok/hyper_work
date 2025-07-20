package hyper.run.domain.user.controller;

import hyper.run.domain.user.service.AuthCodeService;
import hyper.run.domain.user.service.SmsService;
import hyper.run.utils.AuthCodeUtil;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthCodeService authCodeService;
    private final SmsService smsService;

    @PostMapping("/send-code")
    public ResponseEntity<?> sendCode(@RequestParam String phoneNumber) {
        String code = AuthCodeUtil.generate6DigitCode();
        authCodeService.saveAuthCode(phoneNumber, code);
        System.out.println("code === " + code);
        //smsService.sendSms(phoneNumber, "[HYPER.RUN] 인증번호는 " + code + " 입니다.");
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
}