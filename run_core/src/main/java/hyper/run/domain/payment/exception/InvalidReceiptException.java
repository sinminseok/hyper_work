package hyper.run.domain.payment.exception;

/**
 * 유효하지 않은 영수증일 때 발생하는 예외
 */
public class InvalidReceiptException extends RuntimeException {

    public InvalidReceiptException(String message) {
        super(message);
    }

    public InvalidReceiptException(String message, Throwable cause) {
        super(message, cause);
    }
}
