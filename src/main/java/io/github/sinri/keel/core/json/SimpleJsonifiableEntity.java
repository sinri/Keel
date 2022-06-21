package io.github.sinri.keel.core.json;

import io.vertx.core.json.JsonObject;

/**
 * @since 2.7
 */
public class SimpleJsonifiableEntity implements JsonifiableEntity<SimpleJsonifiableEntity> {
    private JsonObject jsonObject;

    public SimpleJsonifiableEntity() {

    }

    public SimpleJsonifiableEntity(JsonObject jsonObject) {
        reloadDataFromJsonObject(jsonObject);
    }

    @Override
    public JsonObject toJsonObject() {
        return jsonObject;
    }

    @Override
    public SimpleJsonifiableEntity reloadDataFromJsonObject(JsonObject jsonObject) {
        this.jsonObject = jsonObject;
        return this;
    }
}
