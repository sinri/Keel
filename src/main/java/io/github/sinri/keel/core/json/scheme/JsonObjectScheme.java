package io.github.sinri.keel.core.json.scheme;

import io.vertx.core.json.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @since 2.7
 */
public class JsonObjectScheme implements JsonElementScheme {

    private final Map<String, JsonElementScheme> elementSchemeMap = new LinkedHashMap<>();
    private boolean strict = true;
    private boolean nullable = false;
    private boolean optional = false;

    public JsonObjectScheme setElementSchemeForKey(String key, JsonElementScheme elementScheme) {
        this.elementSchemeMap.put(key, elementScheme);
        return this;
    }

    public boolean isStrict() {
        return strict;
    }

    public JsonObjectScheme setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject elements = new JsonObject();
        elementSchemeMap.forEach((name, scheme) -> {
            elements.put(name, scheme.toJsonObject());
        });
        return new JsonObject()
                .put("scheme_type", getJsonElementSchemeType())
                .put("nullable", nullable)
                .put("optional", optional)
                .put("strict", strict)
                .put("elements", elements)
                ;
    }

    @Override
    public JsonObjectScheme reloadDataFromJsonObject(JsonObject jsonObject) {
        this.nullable = jsonObject.getBoolean("nullable", false);
        this.optional = jsonObject.getBoolean("optional", false);
        this.strict = jsonObject.getBoolean("strict", true);

        elementSchemeMap.clear();
        JsonObject elements = jsonObject.getJsonObject("elements");
        if (elements != null && !elements.isEmpty()) {
            elements.forEach(elementEntry -> {
                Object value = elementEntry.getValue();
                if (value instanceof JsonObject) {
                    elementSchemeMap.put(elementEntry.getKey(), JsonElementScheme.fromJsonObject((JsonObject) value));
                }
            });
        }
        return this;
    }

    @Override
    public JsonElementSchemeType getJsonElementSchemeType() {
        return JsonElementSchemeType.JsonObject;
    }

    public boolean validate(JsonObject jsonObject) {
        if (jsonObject == null) {
            return isNullable();
        }
        for (var key : this.elementSchemeMap.keySet()) {
            var elementScheme = this.elementSchemeMap.get(key);

            if (elementScheme instanceof JsonObjectScheme) {
                var validated = ((JsonObjectScheme) elementScheme).validate(jsonObject.getJsonObject(key));
                if (!validated) return false;
            } else if (elementScheme instanceof JsonArrayScheme) {
                var validated = ((JsonArrayScheme) elementScheme).validate(jsonObject.getJsonArray(key));
                if (!validated) return false;
            } else if (elementScheme instanceof JsonValueScheme) {
                var validated = ((JsonValueScheme) elementScheme).validate(jsonObject.getValue(key));
                if (!validated) return false;
            } else {
                throw new RuntimeException("SCHEME ERROR");
            }
        }
        if (this.elementSchemeMap.keySet().size() != jsonObject.size()) {
            return !this.strict;
        }
        return true;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    public JsonObjectScheme setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    public JsonObjectScheme setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }
}
