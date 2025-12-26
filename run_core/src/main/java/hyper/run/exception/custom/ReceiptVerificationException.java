package hyper.run.exception.custom;

import hyper.run.exception.ErrorResponseCode;
import hyper.run.exception.ExceptionBase;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ReceiptVerificationException extends ExceptionBase {

    public ReceiptVerificationException(@Nullable String message) {
        this.errorCode = ErrorResponseCode.RECEIPT_VERIFICATION_FAILED;
        this.errorMessage = message;
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }
}
