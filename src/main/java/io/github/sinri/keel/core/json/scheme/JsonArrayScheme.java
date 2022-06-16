package io.github.sinri.keel.core.json.scheme;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @since 2.7
 */
public class JsonArrayScheme extends JsonValueScheme<JsonArray> {
    private final Map<Integer, JsonElementScheme<?>> indexedElementSchemeMap = new LinkedHashMap<>();
    private JsonElementScheme<?> defaultElementScheme;
    private boolean allowEmpty = true;
    private Integer minLength = 0;
    private Integer maxLength = Integer.MAX_VALUE;
//    private boolean nullable = false;
//    private boolean optional = false;

    public JsonArrayScheme setDefaultElementScheme(JsonElementScheme<?> defaultElementScheme) {
        this.defaultElementScheme = defaultElementScheme;
        return this;
    }

    public JsonArrayScheme setElementSchemeForIndex(int index, JsonElementScheme<?> elementScheme) {
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
                .put("nullable", isNullable())
                .put("optional", isOptional())
                .put("default_element", defaultElementScheme)
                .put("indexed_elements", indexTypeMap)
                .put("allow_empty", allowEmpty)
                .put("min_length", minLength)
                .put("max_length", maxLength)
                ;
    }

    @Override
    public JsonElementScheme<JsonArray> reloadDataFromJsonObject(JsonObject jsonObject) {
        super.reloadDataFromJsonObject(jsonObject);
//        this.nullable = jsonObject.getBoolean("nullable", false);
//        this.optional = jsonObject.getBoolean("optional", false);
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
                    JsonElementScheme<?> jsonElementScheme = JsonElementScheme.fromJsonObject((JsonObject) v);
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

//    @Override
//    public boolean isNullable() {
//        return nullable;
//    }

    @Override
    public void digest(JsonArray jsonArray) throws JsonSchemeMismatchException {
        if (jsonArray == null) {
            if (!isNullable()) {
                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleNullableNotAllowed);
            }
            return;
        }
//        if (jsonArray instanceof JsonArray) {
        if (jsonArray.size() == 0) {
            if (!this.isAllowEmpty()) {
                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleEmptyArrayNotAllowed);
            }
        }

        for (Integer i : this.indexedElementSchemeMap.keySet()) {
            JsonElementScheme<?> elementScheme = this.indexedElementSchemeMap.get(i);

            if (i >= 0 && i < jsonArray.size()) {
                try {
                    if (elementScheme instanceof JsonObjectScheme) {
                        ((JsonObjectScheme) elementScheme).digest(jsonArray.getJsonObject(i));
                    } else if (elementScheme instanceof JsonArrayScheme) {
                        ((JsonArrayScheme) elementScheme).digest(jsonArray.getJsonArray(i));
                    } else if (elementScheme instanceof JsonNumberScheme) {
                        ((JsonNumberScheme) elementScheme).digest(jsonArray.getNumber(i));
                    } else if (elementScheme instanceof JsonStringScheme) {
                        ((JsonStringScheme) elementScheme).digest(jsonArray.getString(i));
                    } else if (elementScheme instanceof JsonBooleanScheme) {
                        ((JsonBooleanScheme) elementScheme).digest(jsonArray.getBoolean(i));
                    } else if (elementScheme instanceof JsonNullScheme) {
                        ((JsonNullScheme) elementScheme).digest(jsonArray.getValue(i));
                    } else if (elementScheme instanceof JsonPlainScheme) {
                        ((JsonPlainScheme) elementScheme).digest(jsonArray.getValue(i));
                    } else {
                        throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleSchemeError);
                    }
                } catch (JsonSchemeMismatchException subException) {
                    throw new JsonSchemeMismatchException("INDEX[" + i + "]", subException);
                }
            } else {
                if (!elementScheme.isOptional()) {
                    throw new JsonSchemeMismatchException("INDEX[" + i + "]", JsonSchemeMismatchException.RuleValueLacked);
                }
            }
        }

        for (var i = 0; i < jsonArray.size(); i++) {
            if (this.indexedElementSchemeMap.containsKey(i)) {
                continue;
            }

            JsonElementScheme<?> elementScheme = this.defaultElementScheme;

            try {
                if (elementScheme instanceof JsonObjectScheme) {
                    ((JsonObjectScheme) elementScheme).digest(jsonArray.getJsonObject(i));
                } else if (elementScheme instanceof JsonArrayScheme) {
                    ((JsonArrayScheme) elementScheme).digest(jsonArray.getJsonArray(i));
                } else if (elementScheme instanceof JsonNumberScheme) {
                    ((JsonNumberScheme) elementScheme).digest(jsonArray.getNumber(i));
                } else if (elementScheme instanceof JsonStringScheme) {
                    ((JsonStringScheme) elementScheme).digest(jsonArray.getString(i));
                } else if (elementScheme instanceof JsonBooleanScheme) {
                    ((JsonBooleanScheme) elementScheme).digest(jsonArray.getBoolean(i));
                } else if (elementScheme instanceof JsonNullScheme) {
                    ((JsonNullScheme) elementScheme).digest(jsonArray.getValue(i));
                } else if (elementScheme instanceof JsonPlainScheme) {
                    ((JsonPlainScheme) elementScheme).digest(jsonArray.getValue(i));
                } else {
                    throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleSchemeError);
                }
            } catch (JsonSchemeMismatchException subException) {
                throw new JsonSchemeMismatchException("INDEX[" + i + "]", subException);
            }
        }
//        }else {
//            throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueTypeNotExpected);
//        }
        this.digested = jsonArray;
    }

//    public JsonArrayScheme setNullable(boolean nullable) {
//        this.nullable = nullable;
//        return this;
//    }

    public boolean isAllowEmpty() {
        return allowEmpty;
    }

    public JsonArrayScheme setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
        return this;
    }

//    public void validate(Object jsonArray) throws JsonSchemeMismatchException {
//        if (jsonArray == null) {
//            if (!isNullable()) {
//                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleNullableNotAllowed);
//            }
//            return;
//        }
//        if (jsonArray instanceof JsonArray) {
//            if (((JsonArray) jsonArray).size() == 0) {
//                if (!this.isAllowEmpty()) {
//                    throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleEmptyArrayNotAllowed);
//                }
//            }
//
//            for (Integer i : this.indexedElementSchemeMap.keySet()) {
//                JsonElementScheme<?> elementScheme = this.indexedElementSchemeMap.get(i);
//
//                if (i >= 0 && i < ((JsonArray) jsonArray).size()) {
//                    try {
//                        if (elementScheme instanceof JsonObjectScheme) {
//                            ((JsonObjectScheme) elementScheme).validate(((JsonArray) jsonArray).getJsonObject(i));
//                        } else if (elementScheme instanceof JsonArrayScheme) {
//                            ((JsonArrayScheme) elementScheme).validate(((JsonArray) jsonArray).getJsonArray(i));
//                        }
//                        else if (elementScheme instanceof JsonNumberScheme) {
//                            ((JsonNumberScheme) elementScheme).digest(((JsonArray) jsonArray).getNumber(i));
//                        }
//                        else if (elementScheme instanceof JsonStringScheme) {
//                            ((JsonStringScheme) elementScheme).digest(((JsonArray) jsonArray).getString(i));
//                        }
//                        else if (elementScheme instanceof JsonBooleanScheme) {
//                            ((JsonBooleanScheme) elementScheme).digest(((JsonArray) jsonArray).getBoolean(i));
//                        }
//                        else if (elementScheme instanceof JsonNullScheme) {
//                            ((JsonNullScheme) elementScheme).digest(((JsonArray) jsonArray).getValue(i));
//                        }
//                        else if (elementScheme instanceof JsonPlainScheme) {
//                            ((JsonPlainScheme) elementScheme).digest(((JsonArray) jsonArray).getValue(i));
//                        }
//                        else {
//                            throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleSchemeError);
//                        }
//                    } catch (JsonSchemeMismatchException subException) {
//                        throw new JsonSchemeMismatchException("INDEX[" + i + "]",subException);
//                    }
//                } else {
//                    if (!elementScheme.isOptional()) {
//                        throw new JsonSchemeMismatchException( "INDEX[" + i + "]",JsonSchemeMismatchException.RuleValueLacked);
//                    }
//                }
//            }
//
//            for (var i = 0; i < ((JsonArray) jsonArray).size(); i++) {
//                if (this.indexedElementSchemeMap.containsKey(i)) {
//                    continue;
//                }
//
//                JsonElementScheme<?> elementScheme = this.defaultElementScheme;
//
//                try {
//                    if (elementScheme instanceof JsonObjectScheme) {
//                        ((JsonObjectScheme) elementScheme).validate(((JsonArray) jsonArray).getJsonObject(i));
//                    } else if (elementScheme instanceof JsonArrayScheme) {
//                        ((JsonArrayScheme) elementScheme).validate(((JsonArray) jsonArray).getJsonArray(i));
//                    }
//                    else if (elementScheme instanceof JsonNumberScheme) {
//                        ((JsonNumberScheme) elementScheme).digest(((JsonArray) jsonArray).getNumber(i));
//                    }
//                    else if (elementScheme instanceof JsonStringScheme) {
//                        ((JsonStringScheme) elementScheme).digest(((JsonArray) jsonArray).getString(i));
//                    }
//                    else if (elementScheme instanceof JsonBooleanScheme) {
//                        ((JsonBooleanScheme) elementScheme).digest(((JsonArray) jsonArray).getBoolean(i));
//                    }
//                    else if (elementScheme instanceof JsonNullScheme) {
//                        ((JsonNullScheme) elementScheme).digest(((JsonArray) jsonArray).getValue(i));
//                    }
//                    else if (elementScheme instanceof JsonPlainScheme) {
//                        ((JsonPlainScheme) elementScheme).digest(((JsonArray) jsonArray).getValue(i));
//                    }
//                    else {
//                        throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleSchemeError);
//                    }
//                } catch (JsonSchemeMismatchException subException) {
//                    throw new JsonSchemeMismatchException("INDEX[" + i + "]",subException);
//                }
//            }
//        }else {
//            throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueTypeNotExpected);
//        }
//    }

//    @Override
//    public boolean isOptional() {
//        return optional;
//    }
//
//    public JsonArrayScheme setOptional(boolean optional) {
//        this.optional = optional;
//        return this;
//    }

    public JsonArray getDigested() {
        return this.digested;
    }
}
