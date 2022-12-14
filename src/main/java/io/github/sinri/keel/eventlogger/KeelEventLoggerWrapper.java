package io.github.sinri.keel.eventlogger;

import io.vertx.core.json.JsonObject;

import java.util.function.Supplier;

/**
 * @since 2.9.4
 */
public interface KeelEventLoggerWrapper {
    Supplier<KeelEventLogger> getEventLoggerSupplier();

    String getPresetTopic();

    default void debug(JsonObject event) {
        getEventLoggerSupplier().get().debug(getPresetTopic(), event);
    }

    default void info(JsonObject event) {
        getEventLoggerSupplier().get().info(getPresetTopic(), event);
    }

    default void notice(JsonObject event) {
        getEventLoggerSupplier().get().notice(getPresetTopic(), event);
    }

    default void warning(JsonObject event) {
        getEventLoggerSupplier().get().warning(getPresetTopic(), event);
    }

    default void error(JsonObject event) {
        getEventLoggerSupplier().get().error(getPresetTopic(), event);
    }

    default void fatal(JsonObject event) {
        getEventLoggerSupplier().get().fatal(getPresetTopic(), event);
    }
}
