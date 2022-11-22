package io.github.sinri.keel.maids.watchman;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * @since 2.9.3
 */
public interface KeelWatchmanEventHandler extends Handler<Long> {
    default JsonObject config() {
        return new JsonObject();
    }

//    void setConfig(JsonObject config);

}
