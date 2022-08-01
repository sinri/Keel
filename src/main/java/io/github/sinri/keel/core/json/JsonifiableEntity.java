package io.github.sinri.keel.core.json;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

/**
 * @since 1.14
 */
public interface JsonifiableEntity<E> {
    JsonObject toJsonObject();

    E reloadDataFromJsonObject(JsonObject jsonObject);

    /**
     * @since 2.7
     */
    default <T> T read(Function<JsonPointer, Class<T>> func) {
        JsonPointer jsonPointer = JsonPointer.create();
        Class<T> tClass = func.apply(jsonPointer);
        Object o = jsonPointer.queryJson(toJsonObject());
        if (o == null) {
            return null;
        }
        return tClass.cast(o);
    }

    /**
     * @since 2.7
     */
    default String readString(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return String.class;
        });
    }

    /**
     * @since 2.7
     */
    default Number readNumber(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return Number.class;
        });
    }

    /**
     * @since 2.7
     */
    default Long readLong(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.longValue();
    }

    /**
     * @since 2.7
     */
    default Integer readInteger(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.intValue();
    }

    /**
     * @since 2.7
     */
    default Float readFloat(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.floatValue();
    }

    /**
     * @since 2.7
     */
    default Double readDouble(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.doubleValue();
    }

    /**
     * @since 2.7
     */
    default Boolean readBoolean(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return Boolean.class;
        });
    }

    /**
     * @since 2.7
     */
    default JsonObject readJsonObject(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonObject.class;
        });
    }

    /**
     * @since 2.7
     */
    default JsonArray readJsonArray(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
    }

    /**
     * @since 2.7
     */
    default Object readValue(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return Object.class;
        });
    }

    /**
     * @since 2.7
     */
    default <B extends JsonifiableEntity<?>> B readJsonifiableEntity(Class<B> bClass, String... args) {
        JsonObject jsonObject = readJsonObject(args);
        try {
            return bClass.getConstructor(JsonObject.class).newInstance(jsonObject);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            //e.printStackTrace();
            return null;
        }
    }
}
