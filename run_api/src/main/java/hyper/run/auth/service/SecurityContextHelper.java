package hyper.run.auth.service;

import hyper.run.auth.domain.CustomUserDetails;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * SecurityContext에서 로그인한 사용자 정보를 가져오는 헬퍼 클래스
 */
public class SecurityContextHelper {

    /**
     * 로그인한 사용자의 이메일을 가져옵니다.
     * @return 사용자 이메일
     */
    public static String getLoginEmailBySecurityContext() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * 로그인한 사용자의 ID를 가져옵니다.
     * DB 조회 없이 SecurityContext에서 직접 가져오므로 성능이 우수합니다.
     * @return 사용자 ID
     * @throws IllegalStateException 사용자가 인증되지 않은 경우
     */
    public static Long getLoginUserIdBySecurityContext() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUserId();
        }

        throw new IllegalStateException("User not authenticated or not using CustomUserDetails");
    }

    /**
     * 로그인한 사용자의 전체 정보를 가져옵니다.
     * userId, email 등을 한 번에 가져올 수 있습니다.
     * @return CustomUserDetails
     * @throws IllegalStateException 사용자가 인증되지 않은 경우
     */
    public static CustomUserDetails getLoginUserBySecurityContext() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof CustomUserDetails) {
            return (CustomUserDetails) principal;
        }

        throw new IllegalStateException("User not authenticated or not using CustomUserDetails");
    }
}