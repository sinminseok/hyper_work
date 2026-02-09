package hyper.run.domain.payment.exception;

/**
 * 영수증 검증 실패 시 발생하는 예외
 */
public class ReceiptVerificationException extends RuntimeException {

    public ReceiptVerificationException(String message) {
        super(message);
    }

    public ReceiptVerificationException(String message, Throwable cause) {
        super(message, cause);
    }
}
