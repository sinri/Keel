package io.github.sinri.keel.test.core.json;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

abstract public class EntityWithJsonObject {
    private final JsonObject jsonObject;

    public EntityWithJsonObject(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    protected JsonObject readJsonObject(String key) {
        return this.read(key, JsonObject.class);
    }

    protected JsonArray readJsonArray(String key) {
        return this.read(key, JsonArray.class);
    }

    protected Boolean readBoolean(String key) {
        return this.read(key, Boolean.class);
    }

    protected String readString(String key) {
        return this.read(key, String.class);
    }

    protected Integer readInteger(String key) {
        return this.read(key, Integer.class);
    }

    protected Long readLong(String key) {
        return this.read(key, Long.class);
    }

    protected Float readFloat(String key) {
        return this.read(key, Float.class);
    }

    protected Double readDouble(String key) {
        return this.read(key, Double.class);
    }

    protected Number readNumber(String key) {
        return this.read(key, Number.class);
    }

    protected final <R> R read(String key, Class<R> classOfR) {
        if (JsonObject.class.isAssignableFrom(classOfR)) {
            JsonObject x = this.jsonObject.getJsonObject(key);
            return classOfR.cast(x);
        } else if (JsonArray.class.isAssignableFrom(classOfR)) {
            JsonArray x = this.jsonObject.getJsonArray(key);
            return classOfR.cast(x);
        } else if (Boolean.class.isAssignableFrom(classOfR)) {
            Boolean x = this.jsonObject.getBoolean(key);
            return classOfR.cast(x);
        } else if (String.class.isAssignableFrom(classOfR)) {
            String x = this.jsonObject.getString(key);
            return classOfR.cast(x);
        } else if (Integer.class.isAssignableFrom(classOfR)) {
            Integer x = this.jsonObject.getInteger(key);
            return classOfR.cast(x);
        } else if (Long.class.isAssignableFrom(classOfR)) {
            Long x = this.jsonObject.getLong(key);
            return classOfR.cast(x);
        } else if (Float.class.isAssignableFrom(classOfR)) {
            Float x = this.jsonObject.getFloat(key);
            return classOfR.cast(x);
        } else if (Double.class.isAssignableFrom(classOfR)) {
            Double x = this.jsonObject.getDouble(key);
            return classOfR.cast(x);
        } else if (Number.class.isAssignableFrom(classOfR)) {
            Number x = this.jsonObject.getNumber(key);
            return classOfR.cast(x);
        } else if (EntityWithJsonObject.class.isAssignableFrom(classOfR)) {
            JsonObject x = this.jsonObject.getJsonObject(key);
            try {
                return classOfR.getConstructor(JsonObject.class).newInstance(x);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                return null;
            }
        } else {
            Object x = this.jsonObject.getValue(key);
            return classOfR.cast(x);
        }
    }

    public boolean validate() {
        Method[] declaredMethods = this.getClass().getDeclaredMethods();
        try {
            for (var method : declaredMethods) {
                if (method.getParameterCount() > 0) continue;
                System.out.println("method: " + method);
                Class<?> returnType = method.getReturnType();
                if (
                        JsonObject.class.isAssignableFrom(returnType)
                                || JsonArray.class.isAssignableFrom(returnType)
                                || String.class.isAssignableFrom(returnType)
                                || Boolean.class.isAssignableFrom(returnType)
                                || Long.class.isAssignableFrom(returnType)
                                || Integer.class.isAssignableFrom(returnType)
                                || Float.class.isAssignableFrom(returnType)
                                || Double.class.isAssignableFrom(returnType)
                                || Number.class.isAssignableFrom(returnType)
                                || EntityWithJsonObject.class.isAssignableFrom(returnType)
                                || EntityWithJsonArray.class.isAssignableFrom(returnType)
                ) {
                    method.invoke(this);
                }
            }
            return true;
        } catch (InvocationTargetException | IllegalAccessException | ClassCastException e) {
            //e.printStackTrace();
            return false;
        }
    }
}
