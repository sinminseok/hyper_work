package hyper.run.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 본인 확인 응답 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVerifyResponse {

    private boolean verified;
    private boolean emailMatched;
    private boolean passwordMatched;
    private String message;

    public static UserVerifyResponse success() {
        return UserVerifyResponse.builder()
                .verified(true)
                .emailMatched(true)
                .passwordMatched(true)
                .message("본인 확인 성공")
                .build();
    }

    public static UserVerifyResponse emailMismatch() {
        return UserVerifyResponse.builder()
                .verified(false)
                .emailMatched(false)
                .passwordMatched(true)
                .message("이메일이 일치하지 않습니다.")
                .build();
    }

    public static UserVerifyResponse passwordMismatch() {
        return UserVerifyResponse.builder()
                .verified(false)
                .emailMatched(true)
                .passwordMatched(false)
                .message("비밀번호가 일치하지 않습니다.")
                .build();
    }

    public static UserVerifyResponse bothMismatch() {
        return UserVerifyResponse.builder()
                .verified(false)
                .emailMatched(false)
                .passwordMatched(false)
                .message("이메일과 비밀번호가 모두 일치하지 않습니다.")
                .build();
    }
}
