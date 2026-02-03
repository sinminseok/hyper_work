package hyper.run.dto.user;

import hyper.run.domain.user.entity.LoginType;
import hyper.run.domain.user.entity.Role;
import hyper.run.domain.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailResponse {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private String birth;
    private LoginType loginType;
    private String loginTypeName;
    private Role role;
    private String roleName;
    private int coupon;
    private double point;
    private String profileUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdAtFormatted;
    private String updatedAtFormatted;

    public static AdminUserDetailResponse from(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String createdAtFormatted = user.getCreateDateTime() != null
                ? user.getCreateDateTime().format(formatter)
                : "-";
        String updatedAtFormatted = user.getModifiedDateTime() != null
                ? user.getModifiedDateTime().format(formatter)
                : "-";

        return AdminUserDetailResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .birth(user.getBirth())
                .loginType(user.getLoginType())
                .loginTypeName(user.getLoginType().name())
                .role(user.getRole())
                .roleName(user.getRole().name())
                .coupon(user.getCoupon())
                .point(user.getPoint())
                .profileUrl(user.getProfileUrl())
                .createdAt(user.getCreateDateTime())
                .updatedAt(user.getModifiedDateTime())
                .createdAtFormatted(createdAtFormatted)
                .updatedAtFormatted(updatedAtFormatted)
                .build();
    }
}
