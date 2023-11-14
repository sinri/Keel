package io.github.sinri.keel.core.json.scheme;

import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

public class JsonPlainScheme extends JsonValueScheme<Object> {

    @Override
    public JsonElementSchemeType getJsonElementSchemeType() {
        return JsonElementSchemeType.JsonPlain;
    }

    @Override
    public @Nonnull JsonObject toJsonObject() {
        return super.toJsonObject()
                .put("scheme_type", getJsonElementSchemeType());
    }

    @Override
    public @Nonnull JsonPlainScheme reloadDataFromJsonObject(@Nonnull JsonObject jsonObject) {
        super.reloadDataFromJsonObject(jsonObject);
        return this;
    }
}
