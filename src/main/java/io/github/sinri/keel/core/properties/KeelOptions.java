package io.github.sinri.keel.core.properties;

import io.vertx.core.json.JsonObject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

/**
 * Keel Options as POJO
 * Its `properties`, as POJO, just store the String Values.
 * You may use `get` methods to get them as the expected format.
 */
abstract public class KeelOptions {
    final static public String BOOL_YES = "YES";
    final static public String BOOL_NO = "NO";

    public KeelOptions(JsonObject jsonObject) {
        initializeProperties();
        overwriteProperties(jsonObject);
    }

    abstract protected void initializeProperties();

    public final void overwriteProperties(JsonObject jsonObject) {
        jsonObject.forEach(stringObjectEntry -> {
            try {
                Field field = this.getClass().getField(stringObjectEntry.getKey());
                Object value = stringObjectEntry.getValue();
                if (value instanceof JsonObject) {
                    try {
                        JsonObject x = (JsonObject) value;
                        KeelOptions keelConfigurationPojo = this.getClass().getConstructor(JsonObject.class).newInstance(x);
                        field.set(this, keelConfigurationPojo);
                    } catch (InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                        // just ignore
                    }
                } else {
                    field.set(this, stringObjectEntry.getValue());
                }
            } catch (NoSuchFieldException e) {
                // just ignore!
            } catch (IllegalAccessException e) {
                // just ignore
            }
        });
    }
}
