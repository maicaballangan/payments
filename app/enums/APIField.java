package enums;

import com.google.common.collect.Maps;
import interfaces.Field;
import utils.StringExtensions;

import java.util.Map;
import java.util.Optional;

public enum APIField implements Field {

    ID,
    CLIENT_SECRET,
    CCY,
    AMOUNT,
    EMAIL,
    ORDER,
    CUSTOMER,
    NAME,
    MESSAGE,
    TRANSACTION,
    USERNAME,
    PASSWORD;

    private static final Map<String, Integer> ERROR_MAP = Maps.newHashMap();
    private final String field;
    private final String message;

    static {
        for (APIField k : APIField.values()) {
            ERROR_MAP.put(StringExtensions.upperSnaketoCamelCase(k.name()), k.code());
        }
    }

    APIField() {
        this.field = StringExtensions.upperSnaketoCamelCase(name());
        this.message = field.concat(REQUIRED_ERROR);
    }

    @Override
    public String field() {
        return field;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public Integer code() {
        // Code starts at 0 and ends a 200 (make sure enums don't exceed 200)
        return this.ordinal();
    }

    @Override
    public String toString() {
        return field;
    }

    public static Optional<Integer> code(final String field) {
        return Optional.ofNullable(ERROR_MAP.get(field));
    }
}