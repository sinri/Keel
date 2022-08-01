package io.github.sinri.keel.test.core.json;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

abstract public class EntityWithJsonArray {
    private final JsonArray jsonArray;

    public EntityWithJsonArray(JsonArray jsonArray) {
        this.jsonArray = jsonArray;
    }

    protected JsonObject readJsonObject(int key) {
        return this.read(key, JsonObject.class);
    }

    protected JsonArray readJsonArray(int key) {
        return this.read(key, JsonArray.class);
    }

    protected Boolean readBoolean(int key) {
        return this.read(key, Boolean.class);
    }

    protected String readString(int key) {
        return this.read(key, String.class);
    }

    protected Integer readInteger(int key) {
        return this.read(key, Integer.class);
    }

    protected Long readLong(int key) {
        return this.read(key, Long.class);
    }

    protected Float readFloat(int key) {
        return this.read(key, Float.class);
    }

    protected Double readDouble(int key) {
        return this.read(key, Double.class);
    }

    protected Number readNumber(int key) {
        return this.read(key, Number.class);
    }

    public <R> R read(int key, Class<R> classOfR) {
        if (JsonObject.class.isAssignableFrom(classOfR)) {
            JsonObject x = this.jsonArray.getJsonObject(key);
            return classOfR.cast(x);
        } else if (JsonArray.class.isAssignableFrom(classOfR)) {
            JsonArray x = this.jsonArray.getJsonArray(key);
            return classOfR.cast(x);
        } else if (Boolean.class.isAssignableFrom(classOfR)) {
            Boolean x = this.jsonArray.getBoolean(key);
            return classOfR.cast(x);
        } else if (String.class.isAssignableFrom(classOfR)) {
            String x = this.jsonArray.getString(key);
            return classOfR.cast(x);
        } else if (Integer.class.isAssignableFrom(classOfR)) {
            Integer x = this.jsonArray.getInteger(key);
            return classOfR.cast(x);
        } else if (Long.class.isAssignableFrom(classOfR)) {
            Long x = this.jsonArray.getLong(key);
            return classOfR.cast(x);
        } else if (Float.class.isAssignableFrom(classOfR)) {
            Float x = this.jsonArray.getFloat(key);
            return classOfR.cast(x);
        } else if (Double.class.isAssignableFrom(classOfR)) {
            Double x = this.jsonArray.getDouble(key);
            return classOfR.cast(x);
        } else if (Number.class.isAssignableFrom(classOfR)) {
            Number x = this.jsonArray.getNumber(key);
            return classOfR.cast(x);
        } else if (EntityWithJsonObject.class.isAssignableFrom(classOfR)) {
            JsonObject x = this.jsonArray.getJsonObject(key);
            try {
                return classOfR.getConstructor(JsonObject.class).newInstance(x);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                return null;
            }
        } else {
            Object x = this.jsonArray.getValue(key);
            return classOfR.cast(x);
        }
    }

    public <R> List<R> readAsList(Class<R> classOfR) {
        List<R> list = new ArrayList<>();
        this.jsonArray.forEach(item -> {
            R cast = classOfR.cast(item);
            list.add(cast);
        });
        return list;
    }

    public <R> boolean validate(Class<R> classOfR) {
        AtomicBoolean validated = new AtomicBoolean(true);
        this.jsonArray.forEach(item -> {
            if (!classOfR.isInstance(item)) {
                validated.set(false);
            }
        });
        return validated.get();
    }
}
