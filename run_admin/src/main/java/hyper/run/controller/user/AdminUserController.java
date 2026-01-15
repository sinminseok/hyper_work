package hyper.run.controller.user;

import hyper.run.domain.user.service.UserManagementService;
import hyper.run.dto.user.AdminUserDetailResponse;
import hyper.run.dto.user.AdminUserListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserManagementService userManagementService;

    @GetMapping
    public String userList(
            @RequestParam(value = "searchKeyword", required = false, defaultValue = "") String searchKeyword,
            @RequestParam(value = "sortBy", required = false, defaultValue = "") String sortBy,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model
    ) {
        Page<AdminUserListResponse> users = userManagementService.getUserList(
                searchKeyword, sortBy, page, size
        );

        model.addAttribute("users", users);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("currentUri", "/admin/users");

        return "user/list";
    }

    @GetMapping("/{userId}")
    public String userDetail(@PathVariable Long userId, Model model) {
        AdminUserDetailResponse user = userManagementService.getUserDetail(userId);
        model.addAttribute("user", user);
        model.addAttribute("currentUri", "/admin/users");
        return "user/detail";
    }

    @DeleteMapping("/{userId}")
    @ResponseBody
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userManagementService.deleteUser(userId);
        return ResponseEntity.ok("삭제 완료");
    }
}
