package hyper.run.exception.custom;


import hyper.run.exception.ExceptionBase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.Nullable;
import static hyper.run.exception.ErrorResponseCode.JWT_VALIDATION;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class JwtValidationException extends ExceptionBase {

    public JwtValidationException(@Nullable String message){
        this.errorCode = JWT_VALIDATION;
        this.errorMessage = message;
    }
    @Override
    public int getStatusCode() {
        return HttpStatus.UNAUTHORIZED.value();
    }
}
