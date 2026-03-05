package hyper.run.exception.custom;

import hyper.run.exception.ErrorResponseCode;
import hyper.run.exception.ExceptionBase;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;

public class ActivityTypeConflictException extends ExceptionBase {

    public ActivityTypeConflictException(@NotNull String message) {
        this.errorCode = ErrorResponseCode.ACTIVITY_TYPE_TIME_CONFLICT;
        this.errorMessage = message;
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.CONFLICT.value();
    }
}
