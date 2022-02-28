package io.github.sinri.keel.core.properties;

import io.vertx.core.json.JsonObject;

abstract public class KeelConfigurationBasement {
    private final JsonObject jsonObject;

    public KeelConfigurationBasement(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
    }

    protected JsonObject getJsonObject() {
        return this.jsonObject;
    }
}
