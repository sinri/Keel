package io.github.sinri.keel.core.json.scheme;

import io.github.sinri.keel.core.json.JsonifiableEntity;
import io.vertx.core.json.JsonObject;

/**
 * @since 2.7
 */
interface JsonElementScheme extends JsonifiableEntity<JsonElementScheme> {
    static JsonElementScheme fromJsonObject(JsonObject jsonObject) {
        JsonElementSchemeType scheme_type = JsonElementSchemeType.valueOf(jsonObject.getString("scheme_type"));
        switch (scheme_type) {
            case JsonArray:
                return new JsonArrayScheme().reloadDataFromJsonObject(jsonObject);
            case JsonObject:
                return new JsonObjectScheme().reloadDataFromJsonObject(jsonObject);
            case JsonValue:
                return new JsonValueScheme().reloadDataFromJsonObject(jsonObject);
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

    enum JsonElementSchemeType {
        JsonObject,
        JsonArray,
        JsonValue,
        JsonBoolean,
        JsonNumber,
        JsonString,
        JsonNull
    }
}
