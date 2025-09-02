package hyper.run.exception;

public enum ErrorResponseCode {
    NOT_VALID_TOKEN(4011),
    FAIL_SEND_TOKEN(4012),
    FAIL_LOGIN(4013),
    JWT_VALIDATION(4014),
    NOT_FOUND(4041),
    INVALID_DATA(4042),
    NOT_MATCH_ID(4043),
    DUPLICATED_DATA(5003),
    UPLOAD_S3_ERROR(5004),
    FCM_PUSH(5005),
    GOOGLE_LOCATION(5006),
    POINT_INSUFFICIENT(5007),
    COUPON_INSUFFICIENT(5008),
    S3_NOT_UPLOAD(5010),
    OAUTH_INVALID(5009);

    private final int code;

    ErrorResponseCode(int c) {
        this.code = c;
    }

    public int getCode() {
        return this.code;
    }
}