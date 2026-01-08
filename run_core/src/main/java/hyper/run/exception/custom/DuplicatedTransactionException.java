package hyper.run.exception.custom;

import hyper.run.exception.ErrorResponseCode;
import hyper.run.exception.ExceptionBase;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class DuplicatedTransactionException extends ExceptionBase {

    public DuplicatedTransactionException(@Nullable String message) {
        this.errorCode = ErrorResponseCode.DUPLICATED_TRANSACTION;
        this.errorMessage = message;
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.CONFLICT.value();
    }
}
