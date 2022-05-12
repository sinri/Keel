package io.github.sinri.keel.core.json;

import java.util.function.Function;

/**
 * @since 2.0
 * Experimental
 */
public class JsonObjectItemValidator {
    protected String key;
    protected ExistenceType existenceType;
    protected Function<Object, Boolean> valueValidateFunction;

    public JsonObjectItemValidator(ExistenceType existenceType, String key, Function<Object, Boolean> valueValidateFunction) {
        this.key = key;
        this.existenceType = existenceType;
        this.valueValidateFunction = valueValidateFunction;
    }

    public static boolean validateAsNotNull(Object value) {
        return value != null;
    }

    public static boolean validateAsNotEmptyString(Object value) {
        if (value == null) return false;
        if (value instanceof String) {
            return !((String) value).isEmpty();
        }
        return false;
    }

    public static boolean validateAsInteger(Object value) {
        if (value == null) return false;
        return value instanceof Integer;
    }

    public static boolean validateAsLong(Object value) {
        if (value == null) return false;
        return value instanceof Long;
    }

    public static boolean validateAsFloat(Object value) {
        if (value == null) return false;
        return value instanceof Float;
    }

    public static boolean validateAsDouble(Object value) {
        if (value == null) return false;
        return value instanceof Double;
    }

    public static boolean validateAsBoolean(Object value) {
        if (value == null) return false;
        return value instanceof Boolean;
    }

    public String getKey() {
        return key;
    }

    public ExistenceType getExistenceType() {
        return existenceType;
    }

    public boolean validateValue(Object value) {
        return this.valueValidateFunction.apply(value);
    }

    public enum ExistenceType {
        REQUIRED,
        OPTIONAL,
        FORBIDDEN,
    }
}
