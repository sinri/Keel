package io.github.sinri.keel.core.json.scheme;

import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

/**
 * @since 2.7
 */
public class JsonNullScheme extends JsonValueScheme<Object> {
    public JsonNullScheme() {
        super();
        this.setNullable(true);
    }

    @Override
    public JsonElementSchemeType getJsonElementSchemeType() {
        return JsonElementSchemeType.JsonNull;
    }

    @Override
    public @Nonnull JsonObject toJsonObject() {
        return super.toJsonObject()
                .put("scheme_type", getJsonElementSchemeType());
    }

    @Override
    public @Nonnull JsonElementScheme<Object> reloadDataFromJsonObject(@Nonnull JsonObject jsonObject) {
        return super.reloadDataFromJsonObject(jsonObject);
    }

//    @Override
//    public void validate(Object object) throws JsonSchemeMismatchException {
//        if( object != null){
//            throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
//        }
//    }

    @Override
    public void digest(Object object) throws JsonSchemeMismatchException {
        if (object != null) {
            throw new JsonSchemeMismatchException(JsonSchemeMismatchException.RuleValueNotExpected);
        }
        this.digested = null;
    }

    @Override
    public Object getDigested() {
        return this.digested;
    }
}
