package io.github.sinri.keel.core.helper.runtime;

import io.vertx.core.json.JsonObject;

/**
 * @param <T>
 * @since 2.9.4
 */
public interface RuntimeStatResult<T> {
    long getStatTime();

    T since(T start);

    JsonObject toJsonObject();
}
