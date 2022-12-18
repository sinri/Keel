package io.github.sinri.keel.core.json.scheme;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

public class JsonPlainScheme extends JsonValueScheme<Object> {

    @Override
    public JsonElementSchemeType getJsonElementSchemeType() {
        return JsonElementSchemeType.JsonPlain;
    }

    @Override
    public @NotNull JsonObject toJsonObject() {
        return super.toJsonObject()
                .put("scheme_type", getJsonElementSchemeType());
    }

    @Override
    public @NotNull JsonPlainScheme reloadDataFromJsonObject(JsonObject jsonObject) {
        super.reloadDataFromJsonObject(jsonObject);
        return this;
    }
}
