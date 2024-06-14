package interfaces;

import enums.StatusCode;

public interface Field extends ErrorCode {
    String REQUIRED_ERROR = " must not be empty";

    default Integer statusCode() {
        return StatusCode.PRECONDITION_FAILURE.statusCode();
    }

    default String category() {
        return StatusCode.PRECONDITION_FAILURE.category();
    }

    String field();

    String message();

    Integer code();
}