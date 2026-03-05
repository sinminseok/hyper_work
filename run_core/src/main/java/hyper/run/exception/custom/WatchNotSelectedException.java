package hyper.run.exception.custom;

import hyper.run.exception.ErrorResponseCode;
import hyper.run.exception.ExceptionBase;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class WatchNotSelectedException extends ExceptionBase {

    public WatchNotSelectedException(@Nullable String message) {
        this.errorCode = ErrorResponseCode.WATCH_NOT_SELECTED;
        this.errorMessage = message;
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }
}
