package hyper.run.exception.custom;

import hyper.run.exception.ErrorResponseCode;
import hyper.run.exception.ExceptionBase;
import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class S3UploadException extends ExceptionBase {

    public S3UploadException(@Nullable String message) {
        this.errorCode = ErrorResponseCode.S3_NOT_UPLOAD;
        this.errorMessage = message;
    }

    @Override
    public int getStatusCode() {
        return HttpStatus.NOT_FOUND.value();
    }
}
