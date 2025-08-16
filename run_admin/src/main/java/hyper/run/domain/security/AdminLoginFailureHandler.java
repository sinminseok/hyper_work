package hyper.run.domain.security;


import hyper.run.exception.ErrorResponseCode;
import hyper.run.exception.custom.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class AdminLoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception){
        throw new AuthException(ErrorResponseCode.FAIL_LOGIN, "로그인 실패");
    }
}
