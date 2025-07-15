package hyper.run.domain.user.controller;

import hyper.run.domain.user.dto.UserSignupRequest;
import hyper.run.domain.user.service.UserService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping
    public ResponseEntity<?> save(@RequestBody UserSignupRequest userSignupRequest){
        String encode = passwordEncoder.encode(userSignupRequest.getPassword());
        userService.save(userSignupRequest, encode);
        SuccessResponse response = new SuccessResponse(true, "회원 가입 성공", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping
    public void findLoginId(){

    }

    @PatchMapping
    public void changePassword(){

    }
}
