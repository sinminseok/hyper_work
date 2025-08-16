package hyper.run.exception.custom;

import hyper.run.exception.ErrorResponseCode;
import hyper.run.exception.ExceptionBase;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;

public class UserDuplicatedException extends ExceptionBase {

    public UserDuplicatedException(@Nullable String message) {
        this.errorCode = ErrorResponseCode.DUPLICATED_DATA;
        this.errorMessage = message;
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
