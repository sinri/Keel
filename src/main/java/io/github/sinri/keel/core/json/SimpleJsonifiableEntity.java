package io.github.sinri.keel.core.json;

import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * @since 2.7
 */
public class SimpleJsonifiableEntity implements JsonifiableEntity<SimpleJsonifiableEntity> {
    protected JsonObject jsonObject;

    /**
     * @since 2.8 jsonObject initialized
     */
    public SimpleJsonifiableEntity() {
        this.jsonObject = new JsonObject();
    }

    public SimpleJsonifiableEntity(JsonObject jsonObject) {
        reloadDataFromJsonObject(jsonObject);
    }

    @Override
    public JsonObject toJsonObject() {
        return jsonObject;
    }

    /**
     * @since 2.8 allow jsonObject as null (treated as empty json object)
     */
    @Override
    public SimpleJsonifiableEntity reloadDataFromJsonObject(JsonObject jsonObject) {
        // Objects.requireNonNull(jsonObject);
        this.jsonObject = Objects.requireNonNullElseGet(jsonObject, JsonObject::new);
        return this;
    }

    /**
     * @since 2.8
     */
    @Override
    public String toString() {
        return Objects.requireNonNullElse(toJsonObject(), new JsonObject()).toString();
    }

    @Override
    public SimpleJsonifiableEntity copy() {
        SimpleJsonifiableEntity copied = new SimpleJsonifiableEntity();
        JsonObject copiedJsonObject = this.toJsonObject().copy();
        copied.reloadDataFromJsonObject(copiedJsonObject);
        return copied;
    }
}
