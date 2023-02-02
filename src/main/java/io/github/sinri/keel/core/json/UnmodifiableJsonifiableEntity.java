package io.github.sinri.keel.core.json;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.core.shareddata.Shareable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @since 3.0.0
 */
public interface UnmodifiableJsonifiableEntity extends Iterable<Map.Entry<String, Object>>, Shareable {
    @Nonnull
    JsonObject toJsonObject();

    /**
     * @since 2.7
     * @since 2.8 If java.lang.ClassCastException occurred, return null instead.
     */
    default <T> @Nullable T read(Function<JsonPointer, Class<T>> func) {
        try {
            JsonPointer jsonPointer = JsonPointer.create();
            Class<T> tClass = func.apply(jsonPointer);
            Object o = jsonPointer.queryJson(toJsonObject());
            if (o == null) {
                return null;
            }
            return tClass.cast(o);
        } catch (ClassCastException castException) {
            return null;
        }
    }

    /**
     * @since 2.7
     */
    default @Nullable String readString(String... args) {
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
    default @Nullable Number readNumber(String... args) {
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
    default @Nullable Long readLong(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.longValue();
    }

    /**
     * @since 2.7
     */
    default @Nullable Integer readInteger(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.intValue();
    }

    /**
     * @since 2.7
     */
    default @Nullable Float readFloat(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.floatValue();
    }

    /**
     * @since 2.7
     */
    default @Nullable Double readDouble(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.doubleValue();
    }

    /**
     * @since 2.7
     */
    default @Nullable Boolean readBoolean(String... args) {
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
    default @Nullable JsonObject readJsonObject(String... args) {
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
    default @Nullable JsonArray readJsonArray(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
    }

    /**
     * @since 2.8
     */
    default @Nullable List<JsonObject> readJsonObjectArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<JsonObject> list = new ArrayList<>();
        array.forEach(x -> {
            if (x == null) {
                list.add(null);
            } else if (x instanceof JsonObject) {
                list.add((JsonObject) x);
            } else {
                throw new RuntimeException("NOT JSON OBJECT");
            }
        });
        return list;
    }

    /**
     * @since 2.8
     */
    default @Nullable List<String> readStringArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<String> list = new ArrayList<>();
        array.forEach(x -> {
            if (x == null) {
                list.add(null);
            } else {
                list.add(x.toString());
            }
        });
        return list;
    }

    /**
     * @since 2.8
     */
    default @Nullable List<Integer> readIntegerArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<Integer> list = new ArrayList<>();
        array.forEach(x -> {
            if (x == null) {
                list.add(0);
            } else {
                if (x instanceof Number) {
                    list.add(((Number) x).intValue());
                } else {
                    throw new RuntimeException("Not Integer");
                }
            }
        });
        return list;
    }

    /**
     * @since 2.8
     */
    default @Nullable List<Long> readLongArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<Long> list = new ArrayList<>();
        array.forEach(x -> {
            if (x == null) {
                list.add(0L);
            } else {
                if (x instanceof Number) {
                    list.add(((Number) x).longValue());
                } else {
                    throw new RuntimeException("Not Long");
                }
            }
        });
        return list;
    }

    /**
     * @since 2.8
     */
    default @Nullable List<Float> readFloatArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<Float> list = new ArrayList<>();
        array.forEach(x -> {
            if (x == null) {
                list.add(0.0f);
            } else {
                if (x instanceof Number) {
                    list.add(((Number) x).floatValue());
                } else {
                    throw new RuntimeException("Not Float");
                }
            }
        });
        return list;
    }

    /**
     * @since 2.8
     */
    default @Nullable List<Double> readDoubleArray(String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<Double> list = new ArrayList<>();
        array.forEach(x -> {
            if (x == null) {
                list.add(0.0);
            } else {
                if (x instanceof Number) {
                    list.add(((Number) x).doubleValue());
                } else {
                    throw new RuntimeException("Not Double");
                }
            }
        });
        return list;
    }

    /**
     * @since 2.7
     */
    default @Nullable Object readValue(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return Object.class;
        });
    }

    /**
     * @since 2.8
     */
    default Buffer toBuffer() {
        return toJsonObject().toBuffer();
    }

    @Override
    default Iterator<Map.Entry<String, Object>> iterator() {
        return toJsonObject().iterator();
    }
}
