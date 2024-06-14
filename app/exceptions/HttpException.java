package exceptions;

import com.google.common.collect.Lists;
import com.stripe.net.ApiResource;
import enums.APIField;
import interfaces.ErrorCode;
import org.apache.commons.lang.StringUtils;
import utils.SerializationUtils;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * HttpException
 *
 * @author Maica Ballangan
 * @since v1
 */
public class HttpException extends Exception implements Serializable {

    private static final long serialVersionUID = 1905122041950251208L;

    private final ErrorCode errorCode;
    private final Integer httpStatusCode;
    private final String message;
    private final List<Error> errors = Lists.newArrayList();

    public HttpException(ErrorCode errorCode) {
        super(errorCode.category(), null);
        this.errorCode = errorCode;
        this.httpStatusCode = errorCode.statusCode();
        this.message = errorCode.message();
    }

    public HttpException(Integer httpStatusCode, ErrorCode errorCode) {
        super(errorCode.category(), null);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
        this.message = null;
    }

    public HttpException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.category(), cause);
        this.errorCode = errorCode;
        this.httpStatusCode = errorCode.statusCode();
        this.message = cause.toString();
    }

    public HttpException(ErrorCode errorCode, String message) {
        super(errorCode.category(), null);
        this.errorCode = errorCode;
        this.httpStatusCode = errorCode.statusCode();
        this.message = message;
    }

    public HttpException(Integer httpStatusCode, ErrorCode errorCode, String message) {
        super(errorCode.category(), null);
        this.errorCode = errorCode;
        this.httpStatusCode = httpStatusCode;
        this.message = message;
    }

    public HttpException(ErrorCode errorCode, Map<String, List<play.data.validation.Error>> errorsMap) {
        this(errorCode);
        errorsMap.forEach((k, v) -> {
            Integer theCode = Stream.of(APIField.code(k))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst().orElseThrow(() -> new NullPointerException(k + " is missing from error codes"));
            addError(new Error(theCode, k, v.toString().replaceAll("\\[|\\]", StringUtils.EMPTY)));
        });
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Integer getHttpStatusCode() {
        return httpStatusCode;
    }

    public Integer getCode() {
        return errorCode.code();
    }

    public String getCategory() {
        return errorCode.category();
    }

    @Override
    public String getMessage() {
        return message;
    }

    public boolean hasMessage() {
        return StringUtils.isNotBlank(this.message);
    }

    public List<Error> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !this.errors.isEmpty();
    }

    public void addError(Error error) {
        this.errors.add(error);
    }

    public void removeError(int index) {
        this.errors.remove(index);
    }

    @Override
    public String toString() {
        return String.format("httpCode: %d, code:%d, category:%s, message:%s, errors:%s", getHttpStatusCode(), getCode(),
            getCategory(), getMessage(), getErrors());
    }

    public static class Error implements Serializable {
        private static final long serialVersionUID = 2405172041950251901L;
        private final Integer code;
        private final String field;
        private final String message;

        public Error(Integer code, String field, String message) {
            this.code = code;
            this.field = field;
            this.message = message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Error that = (Error) o;
            return Objects.equals(code, that.code);
        }

        @Override
        public int hashCode() {
            return code.hashCode();
        }

        public Integer getCode() {
            return code;
        }

        public String getField() {
            return field;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return SerializationUtils.serialize(this);
        }
    }
}