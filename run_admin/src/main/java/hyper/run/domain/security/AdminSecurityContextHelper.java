package hyper.run.domain.security;

import org.springframework.security.core.context.SecurityContextHolder;

public class AdminSecurityContextHelper {

    public static String getLoginEmailBySecurityContext() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    public static Long getLoginAdminUserIdBySecurityContext() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof CustomAdminUserDetails) {
            return ((CustomAdminUserDetails) principal).getAdminUserId();
        }

        throw new IllegalStateException("Admin user not authenticated or not using CustomAdminUserDetails");
    }


    public static CustomAdminUserDetails getLoginAdminUserBySecurityContext() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof CustomAdminUserDetails) {
            return (CustomAdminUserDetails) principal;
        }

        throw new IllegalStateException("Admin user not authenticated or not using CustomAdminUserDetails");
    }
}
