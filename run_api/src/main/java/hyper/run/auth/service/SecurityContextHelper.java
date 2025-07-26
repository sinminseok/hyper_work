package hyper.run.auth.service;

import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextHelper {
    public static String getLoginEmailBySecurityContext() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}