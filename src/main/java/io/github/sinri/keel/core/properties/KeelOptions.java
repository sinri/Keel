package io.github.sinri.keel.core.properties;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.sinri.keel.core.KeelHelper;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;

/**
 * Keel Options as POJO
 * Its `properties`, as POJO, just store the String Values.
 * You may use `get` methods to get them as the expected format.
 * <p>
 * As of 1.12, the fields could be other than String with auto cast.
 */
abstract public class KeelOptions {
    final static public String BOOL_YES = "YES";
    final static public String BOOL_NO = "NO";


    public static <T extends KeelOptions> T loadWithYamlFilePath(String yamlFilePath, Class<T> classOfT) throws IOException {
        byte[] bytes = KeelHelper.readFileAsByteArray(yamlFilePath, true);

        var mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();

        return mapper.readValue(new String(bytes, StandardCharsets.UTF_8), classOfT);
    }

    public static <T extends KeelOptions> T loadWithJsonObjectFilePath(String jsonObjectFilePath, Class<T> classOfT) throws IOException {
        byte[] bytes = KeelHelper.readFileAsByteArray(jsonObjectFilePath, true);
        return loadWithJsonObject(new JsonObject(Buffer.buffer(bytes)), classOfT);
    }

    public static <T extends KeelOptions> T loadWithJsonObject(JsonObject jsonObject, Class<T> classOfT) {
        try {
            T options = classOfT.getConstructor().newInstance();
            options.overwritePropertiesWithJsonObject(jsonObject);
            return options;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }
    }

    public final void overwritePropertiesWithJsonObject(JsonObject jsonObject) {
        jsonObject.forEach(stringObjectEntry -> {
            try {
                Field field = this.getClass().getField(stringObjectEntry.getKey());
                Object value = stringObjectEntry.getValue();

                Class<?> type = field.getType();
                if (type == boolean.class || type == Boolean.class) {
                    if (value instanceof Boolean) {
                        field.setBoolean(this, (Boolean) value);
                    } else {
                        field.setBoolean(this, value.toString().equals(BOOL_YES));
                    }
                } else if (type == Byte.class || type == byte.class) {
                    field.setByte(this, Byte.parseByte(value.toString()));
                } else if (type == short.class || type == Short.class) {
                    field.setShort(this, Short.parseShort(value.toString()));
                } else if (type == int.class || type == Integer.class) {
                    field.setInt(this, Integer.parseInt(value.toString()));
                } else if (type == long.class || type == Long.class) {
                    field.setLong(this, Long.parseLong(value.toString()));
                } else if (type == float.class || type == Float.class) {
                    field.setFloat(this, Float.parseFloat(value.toString()));
                } else if (type == double.class || type == Double.class) {
                    field.setDouble(this, Double.parseDouble(value.toString()));
                } else if (KeelOptions.class.isAssignableFrom(type) && value instanceof JsonObject) {
                    try {
                        JsonObject x = (JsonObject) value;
                        field.set(this, type.getConstructor(JsonObject.class).newInstance(x));
                    } catch (InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                        // just ignore
                    }
                } else if (field.getType().isInstance(value)) {
                    field.set(this, value);
                }
                // else ignore
            } catch (NoSuchFieldException | IllegalAccessException e) {
                // just ignore
                e.printStackTrace();
            }
        });
    }
}
