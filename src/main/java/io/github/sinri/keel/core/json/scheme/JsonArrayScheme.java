package io.github.sinri.keel.core.json.scheme;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @since 2.7
 */
public class JsonArrayScheme implements JsonElementScheme {
    private final Map<Integer, JsonElementScheme> indexedElementSchemeMap = new LinkedHashMap<>();
    private JsonElementScheme defaultElementScheme;
    private boolean allowEmpty = true;
    private Integer minLength = 0;
    private Integer maxLength = Integer.MAX_VALUE;
    private boolean nullable = false;
    private boolean optional = false;

    public JsonArrayScheme setDefaultElementScheme(JsonElementScheme defaultElementScheme) {
        this.defaultElementScheme = defaultElementScheme;
        return this;
    }

    public JsonArrayScheme setElementSchemeForIndex(int index, JsonElementScheme elementScheme) {
        this.indexedElementSchemeMap.put(index, elementScheme);
        return this;
    }

    public Integer getMaxLength() {
        return maxLength;
    }

    public JsonArrayScheme setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
        return this;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public JsonArrayScheme setMinLength(Integer minLength) {
        this.minLength = minLength;
        return this;
    }

    @Override
    public JsonObject toJsonObject() {
        JsonObject indexTypeMap = new JsonObject();
        indexedElementSchemeMap.forEach((index, schemeType) -> {
            indexTypeMap.put(String.valueOf(index), schemeType.toJsonObject());
        });
        return new JsonObject()
                .put("scheme_type", getJsonElementSchemeType())
                .put("nullable", nullable)
                .put("optional", optional)
                .put("default_element", defaultElementScheme)
                .put("indexed_elements", indexTypeMap)
                .put("allow_empty", allowEmpty)
                .put("min_length", minLength)
                .put("max_length", maxLength)
                ;
    }

    @Override
    public JsonElementScheme reloadDataFromJsonObject(JsonObject jsonObject) {
        this.nullable = jsonObject.getBoolean("nullable", false);
        this.optional = jsonObject.getBoolean("optional", false);
        this.allowEmpty = jsonObject.getBoolean("allow_empty", true);

        this.minLength = jsonObject.getInteger("min_length", 0);
        this.maxLength = jsonObject.getInteger("max_length", Integer.MAX_VALUE);

        JsonObject default_element = jsonObject.getJsonObject("default_element");
        if (default_element == null) {
            this.defaultElementScheme = null;
        } else {
            this.defaultElementScheme = JsonElementScheme.fromJsonObject(default_element);
        }

        this.indexedElementSchemeMap.clear();
        JsonObject indexed_element_scheme_type_map = jsonObject.getJsonObject("indexed_elements");
        if (indexed_element_scheme_type_map != null) {
            indexed_element_scheme_type_map.forEach(entry -> {
                int i = Integer.parseInt(entry.getKey());
                Object v = entry.getValue();
                if (v instanceof JsonObject) {
                    JsonElementScheme jsonElementScheme = JsonElementScheme.fromJsonObject((JsonObject) v);
                    this.indexedElementSchemeMap.put(i, jsonElementScheme);
                } else {
                    throw new RuntimeException();
                }
            });
        }
        return this;
    }

    @Override
    public JsonElementSchemeType getJsonElementSchemeType() {
        return JsonElementSchemeType.JsonArray;
    }

    @Override
    public boolean isNullable() {
        return nullable;
    }

    public JsonArrayScheme setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public boolean isAllowEmpty() {
        return allowEmpty;
    }

    public JsonArrayScheme setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
        return this;
    }

    public boolean validate(JsonArray jsonArray) {
        if (jsonArray == null) {
            return isNullable();
        }
        if (jsonArray.size() == 0) {
            if (!this.isAllowEmpty()) {
                return false;
            }
        }

        for (Integer i : this.indexedElementSchemeMap.keySet()) {
            JsonElementScheme elementScheme = this.indexedElementSchemeMap.get(i);

            if (i >= 0 && i < jsonArray.size()) {
                if (elementScheme instanceof JsonObjectScheme) {
                    var validated = ((JsonObjectScheme) elementScheme).validate(jsonArray.getJsonObject(i));
                    if (!validated) return false;
                } else if (elementScheme instanceof JsonArrayScheme) {
                    var validated = ((JsonArrayScheme) elementScheme).validate(jsonArray.getJsonArray(i));
                    if (!validated) return false;
                } else if (elementScheme instanceof JsonValueScheme) {
                    var validated = ((JsonValueScheme) elementScheme).validate(jsonArray.getValue(i));
                    if (!validated) return false;
                } else {
                    throw new RuntimeException("SCHEME ERROR");
                }
            } else {
                if (!elementScheme.isOptional()) {
                    return false;
                }
            }
        }

        for (var i = 0; i < jsonArray.size(); i++) {
            if (this.indexedElementSchemeMap.containsKey(i)) {
                continue;
            }

            JsonElementScheme elementScheme = this.defaultElementScheme;

            if (elementScheme instanceof JsonObjectScheme) {
                var validated = ((JsonObjectScheme) elementScheme).validate(jsonArray.getJsonObject(i));
                if (!validated) return false;
            } else if (elementScheme instanceof JsonArrayScheme) {
                var validated = ((JsonArrayScheme) elementScheme).validate(jsonArray.getJsonArray(i));
                if (!validated) return false;
            } else if (elementScheme instanceof JsonValueScheme) {
                var validated = ((JsonValueScheme) elementScheme).validate(jsonArray.getValue(i));
                if (!validated) return false;
            } else {
                throw new RuntimeException("SCHEME ERROR");
            }
        }

        return true;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    public JsonArrayScheme setOptional(boolean optional) {
        this.optional = optional;
        return this;
    }
}
