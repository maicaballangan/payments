package enums;

public enum StatusCode {
    // Error messages
    BAD_REQUEST_ERROR(400, "Bad Request"),
    AUTHENTICATION_ERROR(401, "Authentication error"),
    AUTHORIZATION_ERROR(403, "Authorization error"),
    NOT_FOUND_ERROR(404, "Not Found"),
    NOT_ALLOWED(405, "Not Allowed"),
    CONFLICT_ERROR(409, "Business rule conflict"),
    PRECONDITION_FAILURE(412, "Precondition failure"),
    UNSUPPORTED_MEDIA_TYPE(415, "Unsupported media type"),
    TOO_MANY_REQUESTS(429, "Too many requests"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    private Integer statusCode;
    private String category;

    StatusCode(final Integer statusCode, final String category) {
        this.statusCode = statusCode;
        this.category = category;
    }

    public Integer statusCode() {
        return statusCode;
    }

    public String category() {
        return category;
    }
}