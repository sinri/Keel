package io.github.sinri.keel.core.json;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.core.shareddata.ClusterSerializable;
import io.vertx.core.shareddata.Shareable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @since 1.14
 * @since 2.8 ClusterSerializable: Safe with EventBus Messaging.
 * @since 2.8 Shareable: allows you to put into a LocalMap.
 * @since 2.8 Iterable: you can run forEach with it.
 */
public interface JsonifiableEntity<E> extends ClusterSerializable, Iterable<Map.Entry<String, Object>>, Shareable {
    @Nonnull
    JsonObject toJsonObject();

    @Nonnull
    E reloadDataFromJsonObject(JsonObject jsonObject);

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
    default @Nullable <T extends SimpleJsonifiableEntity> List<T> readEntityArray(Class<T> classOfEntity, String... args) {
        JsonArray array = read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
        if (array == null) return null;
        List<T> list = new ArrayList<>();
        array.forEach(x -> {
            if (x == null) {
                list.add(null);
            } else if (x instanceof JsonObject) {
                try {
                    T t = classOfEntity.getConstructor().newInstance();
                    t.reloadDataFromJsonObject((JsonObject) x);
                    list.add(t);
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
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
     * @param <B> an implementation class of JsonifiableEntity, with constructor B() or B(JsonObject).
     * @since 2.7
     */
    default @Nullable <B extends JsonifiableEntity<?>> B readJsonifiableEntity(Class<B> bClass, String... args) {
        JsonObject jsonObject = readJsonObject(args);
        if (jsonObject == null) return null;
        try {
            var x = bClass.getConstructor().newInstance();
            x.reloadDataFromJsonObject(jsonObject);
            return x;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException ignored) {
        }
        try {
            return bClass.getConstructor(JsonObject.class).newInstance(jsonObject);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException ignored) {
        }
        return null;
    }

    /**
     * @since 2.8
     */
    default Buffer toBuffer() {
        return toJsonObject().toBuffer();
    }

    /**
     * @since 2.8
     */
    default void fromBuffer(Buffer buffer) {
        this.reloadDataFromJsonObject(new JsonObject(buffer));
    }

    /**
     * @since 2.8
     */
    default void writeToBuffer(Buffer buffer) {
        JsonObject jsonObject = this.toJsonObject();
        if (jsonObject == null) {
            jsonObject = new JsonObject();
        }
        jsonObject.writeToBuffer(buffer);
    }

    /**
     * @since 2.8
     */
    default int readFromBuffer(int pos, Buffer buffer) {
        JsonObject jsonObject = new JsonObject();
        int i = jsonObject.readFromBuffer(pos, buffer);
        this.reloadDataFromJsonObject(jsonObject);
        return i;
    }

    @Override
    default Iterator<Map.Entry<String, Object>> iterator() {
        return toJsonObject().iterator();
    }
}
