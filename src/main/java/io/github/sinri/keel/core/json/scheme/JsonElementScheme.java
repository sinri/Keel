package io.github.sinri.keel.core.json.scheme;

import io.github.sinri.keel.core.json.JsonifiableEntity;
import io.vertx.core.json.JsonObject;

/**
 * @since 2.7
 */
public interface JsonElementScheme<T> extends JsonifiableEntity<JsonElementScheme<T>> {
    static JsonElementScheme<?> fromJsonObject(JsonObject jsonObject) {
        JsonElementSchemeType scheme_type = JsonElementSchemeType.valueOf(jsonObject.getString("scheme_type"));
        switch (scheme_type) {
            case JsonArray:
                return new JsonArrayScheme().reloadDataFromJsonObject(jsonObject);
            case JsonObject:
                return new JsonObjectScheme().reloadDataFromJsonObject(jsonObject);
            case JsonPlain:
                return new JsonPlainScheme().reloadDataFromJsonObject(jsonObject);
            case JsonBoolean:
                return new JsonBooleanScheme().reloadDataFromJsonObject(jsonObject);
            case JsonNumber:
                return new JsonNumberScheme().reloadDataFromJsonObject(jsonObject);
            case JsonString:
                return new JsonStringScheme().reloadDataFromJsonObject(jsonObject);
            case JsonNull:
                return new JsonNullScheme().reloadDataFromJsonObject(jsonObject);
        }
        throw new RuntimeException("scheme_type unknown");
    }

    JsonElementSchemeType getJsonElementSchemeType();

    boolean isOptional();

    boolean isNullable();

    void digest(T object) throws JsonSchemeMismatchException;

    T getDigested();

    enum JsonElementSchemeType {
        JsonObject,
        JsonArray,
        JsonPlain,
        JsonBoolean,
        JsonNumber,
        JsonString,
        JsonNull
    }
}
