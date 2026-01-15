package hyper.run.dto.user;

import hyper.run.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class AdminUserListResponse {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private String birth;
    private LocalDateTime createdAt;
    private String createdAtFormatted;

    public static AdminUserListResponse from(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        String createdAtFormatted = user.getCreateDateTime() != null
                ? user.getCreateDateTime().format(formatter)
                : "-";

        return AdminUserListResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .birth(user.getBirth())
                .createdAt(user.getCreateDateTime())
                .createdAtFormatted(createdAtFormatted)
                .build();
    }
}
