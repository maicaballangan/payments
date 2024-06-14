package enums;

import interfaces.ErrorCode;

public enum APIErrorCode implements ErrorCode {
    // Error messages
    VALIDATION_FAILED(StatusCode.BAD_REQUEST_ERROR, "Validation error"),
    ILLEGAL_STATE_ERROR(StatusCode.BAD_REQUEST_ERROR, "Illegal state error"),
    ILLEGAL_ARGUMENT_EXCEPTION(StatusCode.BAD_REQUEST_ERROR, "Illegal argument error"),
    USE_POST_BODY(StatusCode.BAD_REQUEST_ERROR, "Use POST Body instead of querystring"),
    MALFORMED_URL(StatusCode.BAD_REQUEST_ERROR, "Malformed URL"),
    JSON_ERROR(StatusCode.BAD_REQUEST_ERROR, "JSON error"),
    INVALID_PAYLOAD(StatusCode.BAD_REQUEST_ERROR, "Invalid payload"),
    INVALID_SIGNATURE(StatusCode.BAD_REQUEST_ERROR, "Invalid signature"),
    EVENT_TYPE_ERROR(StatusCode.BAD_REQUEST_ERROR, "Unhandled event type"),
    BASIC_AUTHENTICATION_FAILED(StatusCode.AUTHENTICATION_ERROR, "Basic Authentication failed"),
    API_KEY_MISSING(StatusCode.AUTHENTICATION_ERROR, "Api key is missing"),
    API_KEY_INVALID(StatusCode.AUTHENTICATION_ERROR, "Api key is invalid or expired"),
    VERSION_INVALID(StatusCode.AUTHENTICATION_ERROR, "Feature is not available in version"),
    REFERER_MISSING(StatusCode.BAD_REQUEST_ERROR, "Referer header is missing"),
    ORIGIN_MISSING(StatusCode.BAD_REQUEST_ERROR, "Origin header is missing"),
    STRIPE_EXCEPTION(StatusCode.BAD_REQUEST_ERROR, "Stripe transaction error"),
    AUTHORIZATION_FAILED(StatusCode.AUTHORIZATION_ERROR, "You cannot access this resource"),
    LANGUAGE_NOT_FOUND(StatusCode.NOT_FOUND_ERROR, "Language not found"),
    CURRENCY_NOT_FOUND(StatusCode.NOT_FOUND_ERROR, "Currency not found"),
    RESOURCE_NOT_FOUND(StatusCode.NOT_FOUND_ERROR, "Resource not found"),
    IO_ERROR(StatusCode.INTERNAL_SERVER_ERROR, "I/O Error"),
    INTERNAL_SERVER_ERROR(StatusCode.INTERNAL_SERVER_ERROR, "Something has gone wrong on the server");

    // Code starts at 200 and ends at 400 (make sure enums don't exceed 200)
    private static final int INITIAL_ERROR_CD = 200;
    private final StatusCode statusCode;
    private final String message;

    APIErrorCode(final StatusCode statusCode, final String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    @Override
    public Integer statusCode() {
        return statusCode.statusCode();
    }

    @Override
    public String category() {
        return statusCode.category();
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public Integer code() {
        return this.ordinal() + INITIAL_ERROR_CD;
    }
}