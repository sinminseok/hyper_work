package hyper.run.user;

import hyper.run.auth.service.JwtService;
import hyper.run.domain.user.dto.request.UserSignupRequest;
import hyper.run.domain.user.dto.request.UserUpdateRequest;
import hyper.run.domain.user.dto.request.UserVerifyRequest;
import hyper.run.domain.user.dto.request.UserWatchRegisterRequest;
import hyper.run.domain.user.dto.response.UserProfileResponse;
import hyper.run.domain.user.dto.response.UserVerifyResponse;
import hyper.run.domain.user.dto.response.UserWatchConnectedResponse;
import hyper.run.domain.user.dto.response.UserWatchResponse;
import hyper.run.domain.user.dto.response.WatchTokenResponse;
import hyper.run.domain.user.service.UserService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static hyper.run.auth.service.SecurityContextHelper.getLoginEmailBySecurityContext;
import static hyper.run.auth.service.SecurityContextHelper.getLoginUserIdBySecurityContext;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /**
     * 이메일 기반 사용자 회원 가입 API
     */
    @PostMapping
    public ResponseEntity<?> save(@RequestBody UserSignupRequest userSignupRequest){
        String encode = passwordEncoder.encode(userSignupRequest.getPassword());
        userService.save(userSignupRequest, encode);
        SuccessResponse response = new SuccessResponse(true, "회원 가입 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 프로필 수정 API
     */
    @PatchMapping
    public ResponseEntity<?> updateProfile(@RequestBody UserUpdateRequest userUpdateRequest){
        Long userId = getLoginUserIdBySecurityContext();
        userService.updateProfile(userId, userUpdateRequest);
        SuccessResponse response = new SuccessResponse(true, "회원 정보 수정", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 이메일 중복확인 API
     */
    @GetMapping("/email-exists")
    public ResponseEntity<?> checkDuplicatedEmail(@RequestParam String email){
        boolean result = userService.isExistEmail(email);
        SuccessResponse response = new SuccessResponse(true, "중복확인 결과", result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/profile-url")
    public ResponseEntity<?> updateUserImage(@RequestPart(value = "image", required = false) final MultipartFile image) {
        Long userId = getLoginUserIdBySecurityContext();
        userService.updateImage(userId, image);
        SuccessResponse response = new SuccessResponse(true, "프로필 이미지 수정", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 휴대폰 중복 API
     */
    @GetMapping("/phone-exists")
    public ResponseEntity<?> checkDuplicatedPhone(@RequestParam String phoneNumber){
        boolean result = userService.isExistPhoneNumber(phoneNumber);
        SuccessResponse response = new SuccessResponse(true, "중복확인 결과", result);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 사용자 아이디(이메일) 찾기 API
     */
    @GetMapping("/id")
    public ResponseEntity<?>  findLoginId(@RequestParam String phoneNumber){
        String email = userService.findEmailByPhoneNumber(phoneNumber);
        SuccessResponse response = new SuccessResponse(true, "아이디(이메일) 찾기 성공", email);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 내 프로필 조회 API (프로필 변경에서 사용)
     */
    @GetMapping("/my-profile")
    public ResponseEntity<?> getMyProfile(){
        String email = getLoginEmailBySecurityContext();
        UserProfileResponse myProfile = userService.getMyProfile(email);
        SuccessResponse response = new SuccessResponse(true, "내 프로필 조회 성공", myProfile);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 비밀번호 변경 API (TODO @RequestParam 으로 이메일, 비번을 전달받는건 보안상 위험해보임 추후 수정 필요함)
     */
    @PatchMapping("/password")
    public ResponseEntity<?> changePassword(@RequestParam String email, @RequestParam String password){
        userService.editPassword(email, passwordEncoder.encode(password));
        SuccessResponse response = new SuccessResponse(true, "비밀번호 변경 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 내 잔여 쿠폰 개수 구하기 API
     */
    @GetMapping("/coupon")
    public ResponseEntity<?> getMyCouponAmount(){
        String email = getLoginEmailBySecurityContext();
        int couponAmount = userService.getMyCouponAmount(email);
        SuccessResponse response = new SuccessResponse(true, "내 잔여 쿠폰", couponAmount);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 참여권 구매 API
     * todo @RequestParam 에 amount 를 직접 넣어주는건 위험해 보임 수정 필요할듯함
     */
//    @PostMapping("/coupons")
//    public ResponseEntity<?> chargeCoupon(@RequestParam int amount){
//        String email = getLoginEmailBySecurityContext();
//        userService.chargeCoupon(email, amount);
//        SuccessResponse response = new SuccessResponse(true, "쿠폰 충전 완료", null);
//        return new ResponseEntity<>(response, HttpStatus.OK);
//    }

    /**
     * 본인 확인 API
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody UserVerifyRequest request){
        String loginEmail = getLoginEmailBySecurityContext();

        // 비밀번호 매칭은 run_api 모듈에서 수행 (PasswordEncoder 의존성)
        String hashedPassword = userService.getHashedPassword(loginEmail);
        boolean passwordMatched = passwordEncoder.matches(request.getPassword(), hashedPassword);

        UserVerifyResponse verifyResponse = userService.verifyUser(loginEmail, request.getEmail(), passwordMatched);
        SuccessResponse response = new SuccessResponse(verifyResponse.isVerified(), verifyResponse.getMessage(), verifyResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 회원 삭제 API
     */
    @DeleteMapping
    public ResponseEntity<?> deleteUser(){
        String email = getLoginEmailBySecurityContext();
        userService.deleteUser(email);
        SuccessResponse response = new SuccessResponse(true, "회원 탈퇴 완료", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 워치 연동 key 조회 API
     */
    @GetMapping("/watch-connect-information")
    public ResponseEntity<?> findWatchConnectedInformation(){
        String email = getLoginEmailBySecurityContext();
        UserWatchConnectedResponse userWatchConnectedResponse = userService.findUserWatchConnectedResponse(email);
        SuccessResponse response = new SuccessResponse(true, "워치 연결 Key 조회 성공", userWatchConnectedResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 워치 토큰 조회
     */
    @GetMapping("/watch-connect-information/tokens")
    ResponseEntity<?> checkWatchKey(@RequestParam String watchKey){
        String email = userService.getEmailByWatchKey(watchKey);

        String accessToken = jwtService.createAccessToken(email);
        String refreshToken = jwtService.createRefreshToken();

        WatchTokenResponse watchTokenResponse = userService.saveWatchRefreshToken(email, accessToken, refreshToken);

        SuccessResponse response = new SuccessResponse(true, "워치 연결 성공", watchTokenResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 워치 등록 API
     */
    @PostMapping("/watch")
    public ResponseEntity<?> registerUserWatch(@RequestBody UserWatchRegisterRequest request) {
        Long userId = getLoginUserIdBySecurityContext();
        userService.registerUserWatch(userId, request);
        SuccessResponse response = new SuccessResponse(true, "워치 등록 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 워치 정보 조회 API
     */
    @GetMapping("/watch")
    public ResponseEntity<?> getUserWatch() {
        Long userId = getLoginUserIdBySecurityContext();
        UserWatchResponse userWatchResponse = userService.getUserWatch(userId);
        SuccessResponse response = new SuccessResponse(true, "워치 정보 조회 성공", userWatchResponse);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
