package hyper.run.exception;

import lombok.Getter;

@Getter
public abstract class ExceptionBase extends RuntimeException {
    public abstract int getStatusCode();
    protected String errorMessage;
    protected ErrorResponseCode errorCode;

    @Override
    public String getMessage() {
        return errorMessage;
    }
}