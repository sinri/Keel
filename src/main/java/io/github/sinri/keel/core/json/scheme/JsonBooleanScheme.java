package io.github.sinri.keel.core.json.scheme;

import io.vertx.core.json.JsonObject;

/**
 * @since 2.7
 */
public class JsonBooleanScheme extends JsonValueScheme {
    private Boolean expected;

    public JsonBooleanScheme() {
        this.expected = null;
    }

    public boolean getExpected() {
        return expected;
    }

    public JsonBooleanScheme setExpected(boolean expected) {
        this.expected = expected;
        return this;
    }

    @Override
    public JsonElementSchemeType getJsonElementSchemeType() {
        return JsonElementSchemeType.JsonBoolean;
    }

    @Override
    public JsonObject toJsonObject() {
        return super.toJsonObject()
                .put("scheme_type", getJsonElementSchemeType())
                .put("expected", expected);
    }

    @Override
    public JsonElementScheme reloadDataFromJsonObject(JsonObject jsonObject) {
        super.reloadDataFromJsonObject(jsonObject);
        this.expected = jsonObject.getBoolean("expected");
        return this;
    }

    public boolean validate(Object object) {
        if (object == null) {
            return isNullable();
        }
        if (object instanceof Boolean) {
            if (expected == null) return true;
            return object == expected;
        }
        return false;
    }
}
