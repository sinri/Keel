package io.github.sinri.keel.core.json.scheme;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * @since 2.7
 */
public class JsonBooleanScheme extends JsonValueScheme<Boolean> {
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
    public @NotNull JsonObject toJsonObject() {
        return super.toJsonObject()
                .put("scheme_type", getJsonElementSchemeType())
                .put("expected", expected);
    }

    @Override
    public @NotNull JsonElementScheme<Boolean> reloadDataFromJsonObject(JsonObject jsonObject) {
        super.reloadDataFromJsonObject(jsonObject);
        this.expected = jsonObject.getBoolean("expected");
        return this;
    }

//    public void validate(Object object) throws JsonSchemeMismatchException {
//        if (object == null) {
//            if (!isNullable()) {
//                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleNullableNotAllowed);
//            }
//        }
//        if (object instanceof Boolean) {
//            if (expected != null) {
//                if (object != expected) {
//                    throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
//                }
//            }
//        }else {
//            throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueTypeNotExpected);
//        }
//    }

    @Override
    public void digest(Boolean object) throws JsonSchemeMismatchException {
        if (object == null) {
            if (!isNullable()) {
                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleNullableNotAllowed);
            }
            return;
        }
//        if (object instanceof Boolean) {
        if (expected != null) {
            if (object != expected) {
                throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
            }
        }
//        }else {
//            throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueTypeNotExpected);
//        }
        this.digested = object;
    }

    @Override
    public Boolean getDigested() {
        return digested;
    }
}
