package io.github.sinri.keel.core.logger2;

import io.vertx.core.json.JsonObject;

/**
 * Print nothing.
 */
public class KeelSilentLogger implements KeelLogger {
    private static final KeelSilentLogger instance = new KeelSilentLogger();

    /**
     * declared as singleton
     */
    private KeelSilentLogger() {
    }

    public static KeelSilentLogger getInstance() {
        return instance;
    }

    @Override
    public String getUniqueLoggerID() {
        return getClass().getName();
    }

    @Override
    public KeelLogger setCategoryPrefix(String categoryPrefix) {
        return this;
    }

    @Override
    public void debug(String msg) {

    }

    @Override
    public void debug(String msg, JsonObject context) {

    }

    @Override
    public void info(String msg) {

    }

    @Override
    public void info(String msg, JsonObject context) {

    }

    @Override
    public void notice(String msg) {

    }

    @Override
    public void notice(String msg, JsonObject context) {

    }

    @Override
    public void warning(String msg) {

    }

    @Override
    public void warning(String msg, JsonObject context) {

    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void error(String msg, JsonObject context) {

    }

    @Override
    public void fatal(String msg) {

    }

    @Override
    public void fatal(String msg, JsonObject context) {

    }

    @Override
    public void exception(Throwable throwable) {

    }

    @Override
    public void exception(String msg, Throwable throwable) {

    }

    @Override
    public void exception(KeelLogLevel level, String msg, Throwable throwable) {

    }

    @Override
    public void text(String text) {

    }

    @Override
    public void text(String text, String lineEnding) {

    }

    @Override
    public void text(KeelLogLevel logLevel, String text, String lineEnding) {

    }

    @Override
    public void reportCurrentRuntimeCodeLocation(String remark) {

    }
}
