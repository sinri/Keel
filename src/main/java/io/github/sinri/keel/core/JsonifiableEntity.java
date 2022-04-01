package io.github.sinri.keel.core;

import io.vertx.core.json.JsonObject;

/**
 * @param <T>
 * @since 1.14
 */
public interface JsonifiableEntity<T> {
    JsonObject toJsonObject();

    T reloadDataFromJsonObject(JsonObject jsonObject);
}
