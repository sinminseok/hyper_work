package hyper.run.api;

import hyper.run.domain.user.dto.response.UserAdminResponse;
import hyper.run.domain.user.service.AdminUserService;
import hyper.run.domain.user.service.UserService;
import hyper.run.utils.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api/admin/users")
public class AdminUserController {

    private final AdminUserService userService;

    @GetMapping
    public ResponseEntity<?> searchUsers(@RequestParam(required = false) String searchCategory,
                                         @RequestParam(required = false) String keyword,
                                         @PageableDefault(size = 6) Pageable pageable) {
        Page<UserAdminResponse> users = userService.searchUsers(searchCategory, keyword, pageable);
        SuccessResponse response = new SuccessResponse(true,"조건별 사용자 조회 성공",users);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{email}")
    public ResponseEntity<?> delete(@PathVariable String email){
        userService.deleteUser(email);
        SuccessResponse response = new SuccessResponse(true, "회원 삭제 완료", null);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
