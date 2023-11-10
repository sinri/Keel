package io.github.sinri.keel.core.json.scheme;

import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @since 2.7
 */
public class JsonObjectScheme extends JsonValueScheme<JsonObject> {

    private final Map<String, JsonElementScheme<?>> elementSchemeMap = new LinkedHashMap<>();
    private boolean strict = true;
//    private boolean nullable = false;
//    private boolean optional = false;

    public JsonObjectScheme setElementSchemeForKey(String key, JsonElementScheme<?> elementScheme) {
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
    public @Nonnull JsonObject toJsonObject() {
        JsonObject elements = new JsonObject();
        elementSchemeMap.forEach((name, scheme) -> {
            elements.put(name, scheme.toJsonObject());
        });
        return new JsonObject()
                .put("scheme_type", getJsonElementSchemeType())
                .put("nullable", isNullable())
                .put("optional", isOptional())
                .put("strict", strict)
                .put("elements", elements)
                ;
    }

    @Override
    public @Nonnull JsonObjectScheme reloadDataFromJsonObject(@Nonnull JsonObject jsonObject) {
        super.reloadDataFromJsonObject(jsonObject);
//        this.nullable = jsonObject.getBoolean("nullable", false);
//        this.optional = jsonObject.getBoolean("optional", false);
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

//    public void validate(Object jsonObject) throws JsonSchemeMismatchException {
//        if (jsonObject == null) {
//            if (!isNullable()) {
//                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleNullableNotAllowed);
//            }
//            return;
//        }
//        if (jsonObject instanceof JsonObject) {
//            for (var key : this.elementSchemeMap.keySet()) {
//                var elementScheme = this.elementSchemeMap.get(key);
//
//                try {
//                    if (elementScheme instanceof JsonObjectScheme) {
//                        ((JsonObjectScheme) elementScheme).digest(((JsonObject) jsonObject).getJsonObject(key));
//                    } else if (elementScheme instanceof JsonArrayScheme) {
//                        ((JsonArrayScheme) elementScheme).digest(((JsonObject) jsonObject).getJsonArray(key));
//                    }
//                    else if (elementScheme instanceof JsonStringScheme) {
//                        ((JsonStringScheme) elementScheme).digest(((JsonObject) jsonObject).getString(key));
//                    }
//                    else if (elementScheme instanceof JsonNumberScheme) {
//                        ((JsonNumberScheme) elementScheme).digest(((JsonObject) jsonObject).getNumber(key));
//                    }
//                    else if (elementScheme instanceof JsonBooleanScheme) {
//                        ((JsonBooleanScheme) elementScheme).digest(((JsonObject) jsonObject).getBoolean(key));
//                    }
//                    else if (elementScheme instanceof JsonNullScheme) {
//                        ((JsonNullScheme) elementScheme).digest(((JsonObject) jsonObject).getValue(key));
//                    }
//                    else if (elementScheme instanceof JsonPlainScheme) {
//                        ((JsonPlainScheme) elementScheme).digest(((JsonObject) jsonObject).getValue(key));
//                    } else {
////                        System.out.println("actual: "+elementScheme.getClass().getName());
//                        throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleSchemeError);
//                    }
//                } catch (JsonSchemeMismatchException subError) {
//                    throw new JsonSchemeMismatchException("KEY[" + key + "]",subError);
//                }
//            }
//            if (this.elementSchemeMap.keySet().size() != ((JsonObject) jsonObject).size()) {
//                if (this.strict) {
//                    throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleFieldsMismatchInStrictMode);
//                }
//            }
//        }else {
//            throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueTypeNotExpected);
//        }
//    }

//    @Override
//    public boolean isNullable() {
//        return nullable;
//    }

    @Override
    public void digest(JsonObject jsonObject) throws JsonSchemeMismatchException {
        if (jsonObject == null) {
            if (!isNullable()) {
                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleNullableNotAllowed);
            }
            return;
        }

        for (var key : this.elementSchemeMap.keySet()) {
            var elementScheme = this.elementSchemeMap.get(key);

            try {
                if (elementScheme instanceof JsonObjectScheme) {
                    ((JsonObjectScheme) elementScheme).digest(jsonObject.getJsonObject(key));
                } else if (elementScheme instanceof JsonArrayScheme) {
                    ((JsonArrayScheme) elementScheme).digest(jsonObject.getJsonArray(key));
                } else if (elementScheme instanceof JsonStringScheme) {
                    ((JsonStringScheme) elementScheme).digest(jsonObject.getString(key));
                } else if (elementScheme instanceof JsonNumberScheme) {
                    ((JsonNumberScheme) elementScheme).digest(jsonObject.getNumber(key));
                } else if (elementScheme instanceof JsonBooleanScheme) {
                    ((JsonBooleanScheme) elementScheme).digest(jsonObject.getBoolean(key));
                } else if (elementScheme instanceof JsonNullScheme) {
                    ((JsonNullScheme) elementScheme).digest(jsonObject.getValue(key));
                } else if (elementScheme instanceof JsonPlainScheme) {
                    ((JsonPlainScheme) elementScheme).digest(jsonObject.getValue(key));
                } else {
//                        System.out.println("actual: "+elementScheme.getClass().getName());
                    throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleSchemeError);
                }
            } catch (JsonSchemeMismatchException subError) {
                throw new JsonSchemeMismatchException("KEY[" + key + "]", subError);
            }
        }
        if (this.elementSchemeMap.keySet().size() != jsonObject.size()) {
            if (this.strict) {
                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleFieldsMismatchInStrictMode);
            }
        }

        this.digested = jsonObject;
    }

//    public JsonObjectScheme setNullable(boolean nullable) {
//        this.nullable = nullable;
//        return this;
//    }

//    @Override
//    public boolean isOptional() {
//        return optional;
//    }
//
//    public JsonObjectScheme setOptional(boolean optional) {
//        this.optional = optional;
//        return this;
//    }


    @Override
    public JsonObject getDigested() {
        return digested;
    }
}
