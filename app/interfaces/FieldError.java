package interfaces;

public interface FieldError {
    String REQUIRED_ERROR = " must not be empty";

    String message();

    Integer code();
}