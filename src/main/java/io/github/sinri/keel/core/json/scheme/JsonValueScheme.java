package io.github.sinri.keel.core.json.scheme;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @since 2.7
 */
public class JsonValueScheme implements JsonElementScheme {

    private boolean nullable = false;
    private boolean optional = false;

    @Override
    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("scheme_type", getJsonElementSchemeType())
                .put("nullable", nullable)
                .put("optional", optional)
                ;
    }

    @Override
    public JsonElementScheme reloadDataFromJsonObject(JsonObject jsonObject) {
        this.nullable = jsonObject.getBoolean("nullable", false);
        this.optional = jsonObject.getBoolean("optional", false);
        return this;
    }

    @Override
    public JsonElementSchemeType getJsonElementSchemeType() {
        return JsonElementSchemeType.JsonValue;
    }

    public boolean validate(Object object) {
        if (object instanceof JsonObject || object instanceof JsonArray) {
            return false;
        } else {
            if (object == null) {
                return isNullable();
            } else {
                // not null
                return true;
            }
        }
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    public JsonValueScheme setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    public JsonValueScheme setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }
}
