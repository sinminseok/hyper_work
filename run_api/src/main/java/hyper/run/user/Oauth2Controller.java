package hyper.run.user;

import hyper.run.auth.service.JwtService;
import hyper.run.domain.user.service.UserService;
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
@RequestMapping
public class Oauth2Controller {

//    private final AppleAuthService appleAuthService;
//    private final UserService userService;
//    private final GoogleAuthService googleAuthService;
//    private final JwtService jwtService;
//
//
//    @PostMapping(GOOGLE_LOGIN_URL)
//    public ResponseEntity<?> authenticateWithGoogle(@RequestBody final GoogleAuthRequest request) throws GeneralSecurityException, IOException {
//        String email = googleAuthService.getGoogleEmail(request);
//        if (!userService.isExistAccountByEmail(email)) {
//            SuccessResponse successResponse = new SuccessResponse(false, REQUEST_SIGN_UP_MESSAGE, email);
//            return new ResponseEntity<>(successResponse, HttpStatus.OK);
//        }
//        SuccessResponse successResponse = new SuccessResponse(true, JWT_GENERATOR_MESSAGE, createLoginResponse(email));
//        return new ResponseEntity<>(successResponse, HttpStatus.OK);
//    }
//
//
//    @PostMapping(APPLE_LOGIN_URL)
//    public ResponseEntity<?> authenticateWithApple(@RequestBody final String identityToken) throws IOException, ParseException, JOSEException {
//        String email = appleAuthService.getAppleEmail(identityToken);
//        if (!userService.isExistAccountByEmail(email)) {
//            SuccessResponse successResponse = new SuccessResponse(false, REQUEST_SIGN_UP_MESSAGE, email);
//            return new ResponseEntity<>(successResponse, HttpStatus.OK);
//        }
//        SuccessResponse successResponse = new SuccessResponse(true, JWT_GENERATOR_MESSAGE, createLoginResponse(email));
//        return new ResponseEntity<>(successResponse, HttpStatus.OK);
//    }
//
//    private LoginResponse createLoginResponse(final String email) {
//        String accessToken = jwtService.createAccessToken(email);
//        String refreshToken = jwtService.createRefreshToken();
//        userService.updateRefreshToken(email, refreshToken);
//        return LoginResponse.builder()
//                .accessToken(accessToken)
//                .refreshToken(refreshToken)
//                .build();
//    }
}