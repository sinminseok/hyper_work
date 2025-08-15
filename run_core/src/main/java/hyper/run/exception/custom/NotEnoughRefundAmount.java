package hyper.run.exception.custom;

import hyper.run.exception.ErrorResponseCode;
import hyper.run.exception.ExceptionBase;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;

public class NotEnoughRefundAmount extends ExceptionBase {

    public NotEnoughRefundAmount(@Nullable String message) {
        this.errorCode = ErrorResponseCode.POINT_INSUFFICIENT;
        this.errorMessage = message;
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}