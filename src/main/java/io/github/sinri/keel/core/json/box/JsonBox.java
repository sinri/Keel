package io.github.sinri.keel.core.json.box;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

/**
 * @since 2.7
 */
abstract public class JsonBox extends JsonObject {
    public JsonBox() {
        super();
    }

    public JsonBox(String json) {
        super(json);
    }

    public JsonBox(JsonObject jsonObject) {
        super();
        jsonObject.forEach(entry -> this.put(entry.getKey(), entry.getValue()));
    }

    /**
     * 检验当前JSON对象是否符合要求。
     */
    abstract public boolean validate();

    private <T> T read(Function<JsonPointer, Class<T>> func) {
        JsonPointer jsonPointer = JsonPointer.create();
        Class<T> tClass = func.apply(jsonPointer);
        Object o = jsonPointer.queryJson(this);
        if (o == null) {
            return null;
        }
        return tClass.cast(o);
    }

    public String readString(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return String.class;
        });
    }

    public Number readNumber(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return Number.class;
        });
    }

    public Long readLong(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.longValue();
    }

    public Integer readInteger(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.intValue();
    }

    public Float readFloat(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.floatValue();
    }

    public Double readDouble(String... args) {
        Number number = readNumber(args);
        if (number == null) return null;
        return number.doubleValue();
    }

    public Boolean readBoolean(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return Boolean.class;
        });
    }

    public JsonObject readJsonObject(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonObject.class;
        });
    }

    public JsonArray readJsonArray(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return JsonArray.class;
        });
    }

    public Object readValue(String... args) {
        return read(jsonPointer -> {
            for (var arg : args) {
                jsonPointer.append(arg);
            }
            return Object.class;
        });
    }

    public <B extends JsonBox> B readBox(Class<B> bClass, String... args) {
        JsonObject jsonObject = readJsonObject(args);
        try {
            return bClass.getConstructor(JsonObject.class).newInstance(jsonObject);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }
}
