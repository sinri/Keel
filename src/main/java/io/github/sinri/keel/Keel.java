package io.github.sinri.keel;

import io.github.sinri.keel.core.controlflow.FutureForEach;
import io.github.sinri.keel.core.controlflow.FutureForRange;
import io.github.sinri.keel.core.controlflow.FutureSleep;
import io.github.sinri.keel.core.controlflow.FutureUntil;
import io.github.sinri.keel.core.helper.KeelHelpers;
import io.github.sinri.keel.core.logger.KeelLogLevel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.core.logger.KeelLoggerOptions;
import io.github.sinri.keel.core.properties.KeelPropertiesReader;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.github.sinri.keel.mysql.KeelMySQLOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class Keel {
    private static final KeelPropertiesReader propertiesReader = new KeelPropertiesReader();
    private static final Map<String, KeelMySQLKit> mysqlKitMap = new HashMap<>();

    private static Vertx vertx;

    public static void loadPropertiesFromFile(String propertiesFileName) {
        propertiesReader.appendPropertiesFromFile(propertiesFileName);
    }

    public static KeelPropertiesReader getPropertiesReader() {
        return propertiesReader;
    }

    /**
     * @param vertxOptions VertxOptions
     * @see <a href="https://vertx.io/docs/apidocs/io/vertx/core/VertxOptions.html">Class VertxOptions</a>
     */
    public static void initializeVertx(VertxOptions vertxOptions) {
        vertx = Vertx.vertx(vertxOptions);
    }

    public static Vertx getVertx() {
        if (vertx == null) {
            throw new RuntimeException("The shared vertx instance was not initialized. Run `Keel.initializeVertx()` first!");
        }
        return vertx;
    }

    /**
     * @since 2.1
     * @deprecated since 2.3
     */
    @Deprecated(since = "2.3", forRemoval = true)
    public static void closeVertx() {
        if (vertx != null) {
            vertx.close();
        }
    }

    public static EventBus getEventBus() {
        return vertx.eventBus();
    }

    /**
     * @param aspect the aspect
     * @return a new KeelLogger instance (would not be shared)
     * @since 1.11
     */
    public static KeelLogger standaloneLogger(String aspect) {
        KeelLoggerOptions options = new KeelLoggerOptions()
                .addIgnorableStackPackage("io.vertx,io.netty,java.lang")
                .loadForAspect(aspect);
        return KeelLogger.createLogger(options);
    }

    public static KeelLogger outputLogger(String aspect) {
        KeelLoggerOptions options = new KeelLoggerOptions()
                .setCompositionStyle(KeelLoggerOptions.CompositionStyle.THREE_LINES)
                .addIgnorableStackPackage("io.vertx,io.netty,java.lang")
                .loadForAspect(aspect)
                .setImplement("print");
        return KeelLogger.createLogger(options);
    }

    /**
     * This method to get output logger, with all options could be overwritten.
     *
     * @since 2.9
     */
    public static KeelLogger outputLogger(String aspect, Handler<KeelLoggerOptions> optionsHandler) {
        KeelLoggerOptions options = new KeelLoggerOptions()
                .setCompositionStyle(KeelLoggerOptions.CompositionStyle.THREE_LINES)
                .addIgnorableStackPackage("io.vertx,io.netty,java.lang")
                .loadForAspect(aspect)
                .setImplement("print");
        if (optionsHandler != null) optionsHandler.handle(options);
        return KeelLogger.createLogger(options);
    }

    /**
     * @since 2.9
     */
    public static KeelLogger outputLogger(Handler<KeelLoggerOptions> optionsHandler) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 3) {
            return outputLogger("unknown");
        } else {
            StackTraceElement st = stackTrace[2];
            return outputLogger(st.getClassName() + "::" + st.getMethodName(), optionsHandler)
                    .setCategoryPrefix("{" + st.getFileName() + ":" + st.getLineNumber() + "}");
        }
    }

    /**
     * @since 2.9
     */
    public static KeelLogger outputLogger() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 3) {
            return outputLogger("unknown", keelLoggerOptions -> keelLoggerOptions.setLowestVisibleLogLevel(KeelLogLevel.DEBUG));
        } else {
            StackTraceElement st = stackTrace[2];
            return outputLogger(Keel.class.getName(), keelLoggerOptions -> keelLoggerOptions.setLowestVisibleLogLevel(KeelLogLevel.DEBUG))
                    .setContentPrefix(st.toString());
        }
    }

    public static KeelMySQLKit getMySQLKit(String dataSourceName) {
        if (!mysqlKitMap.containsKey(dataSourceName)) {
            KeelMySQLOptions keelMySQLOptions = KeelMySQLOptions.generateOptionsForDataSourceWithPropertiesReader(dataSourceName);
            KeelMySQLKit keelMySQLKit = new KeelMySQLKit(keelMySQLOptions);
            mysqlKitMap.put(dataSourceName, keelMySQLKit);
        }
        return mysqlKitMap.get(dataSourceName);
    }

    /**
     * @return getMySQLKit(mysql.default_data_source_name);
     * @since 1.10
     */
    public static KeelMySQLKit getMySQLKit() {
        String defaultName = propertiesReader.getProperty("mysql.default_data_source_name");
        return getMySQLKit(defaultName);
    }

    /**
     * @since 2.9
     */
    public static KeelHelpers helpers() {
        return KeelHelpers.getInstance();
    }

    /**
     * @since 2.9
     */
    public static <R> Future<R> executeWithinLock(String lockName, Supplier<Future<R>> supplier) {
        return executeWithinLock(lockName, 10_000L, supplier);
    }

    /**
     * @since 2.9
     */
    public static <R> Future<R> executeWithinLock(String lockName, long timeout, Supplier<Future<R>> supplier) {
        return getVertx().sharedData().getLockWithTimeout(lockName, timeout)
                .compose(lock -> Future.succeededFuture()
                        .compose(v -> supplier.get())
                        .onComplete(ar -> lock.release()));
    }

    /**
     * @since 2.9
     */
    public static Future<Void> callFutureUntil(Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier) {
        return FutureUntil.call(singleRecursionForShouldStopSupplier);
    }

    /**
     * @since 2.9
     */
    public static <T> Future<Void> callFutureForEach(Iterable<T> collection, Function<T, Future<Void>> itemProcessor) {
        return FutureForEach.call(collection, itemProcessor);
    }

    /**
     * @since 2.9
     */
    public static Future<Void> callFutureForRange(FutureForRange.Options options, Function<Integer, Future<Void>> handleFunction) {
        return FutureForRange.call(options, handleFunction);
    }

    /**
     * @since 2.9
     */
    public static Future<Void> callFutureForRange(int times, Function<Integer, Future<Void>> handleFunction) {
        return FutureForRange.call(times, handleFunction);
    }

    /**
     * @since 2.9
     */
    public static Future<Void> callFutureSleep(long t) {
        return FutureSleep.call(t);
    }
}
