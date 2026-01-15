package hyper.run.exception.custom;

import hyper.run.exception.ErrorResponseCode;
import hyper.run.exception.ExceptionBase;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;

public class AlreadyApplyGameException extends ExceptionBase {

    public AlreadyApplyGameException(@NotNull String message) {
        this.errorCode = ErrorResponseCode.ALREADY_EXIST;
        this.errorMessage = message;
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.CONFLICT.value();
    }
}
