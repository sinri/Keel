package io.github.sinri.keel.logger.event.legacy.center;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.event.legacy.KeelEventLog;
import io.github.sinri.keel.logger.event.legacy.KeelEventLogImpl;
import io.github.sinri.keel.logger.event.legacy.KeelEventLogger;
import io.github.sinri.keel.logger.event.legacy.adapter.OutputAdapter;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Function;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 3.0.0
 */
@Deprecated(since = "3.2.0", forRemoval = true)
public class KeelOutputEventLogCenter extends KeelSyncEventLogCenter {
    /**
     * @since 3.0.10 Change to use KeelEventLog::render
     */
    private final static KeelOutputEventLogCenter defaultInstance = new KeelOutputEventLogCenter(
//    null
            KeelEventLog::render
    );
    private static Set<String> mixedStackPrefixSet = Set.of(
            "io.vertx.",
            "java.",
            "io.netty."
    );

    private KeelOutputEventLogCenter(@Nullable Function<KeelEventLog, Future<String>> converter) {
        super(OutputAdapter.getInstance(converter));
    }

    public static KeelOutputEventLogCenter getInstance() {
        return defaultInstance;
    }

    public static KeelOutputEventLogCenter getInstance(@Nullable Function<KeelEventLog, Future<String>> converter) {
        return new KeelOutputEventLogCenter(converter);
    }

    /**
     * @param mixedStackPrefixSet name prefix of class to ignore
     * @since 3.0.0
     */
    public static void setMixedStackPrefixSet(@Nonnull Set<String> mixedStackPrefixSet) {
        KeelOutputEventLogCenter.mixedStackPrefixSet = mixedStackPrefixSet;
    }

    @Nonnull
    public static KeelEventLogger instantLogger() {
        var logCenter = getInstance(KeelEventLog::render);
        return logCenter.createLogger("INSTANT", () -> {
            JsonArray array;
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace.length <= 5) {
                array = null;
            } else {
                array = new JsonArray();

                var slice = Arrays.copyOfRange(stackTrace, 5, stackTrace.length);
                KeelHelpers.jsonHelper().filterStackTrace(
                        slice,
                        mixedStackPrefixSet,
                        (currentPrefix, ps) -> array.add(currentPrefix + " × " + ps),
                        stackTraceElement -> array.add(stackTraceElement.toString())
                );
            }
            return new InstantLog("INSTANT", array);
        });
    }

    private static class InstantLog extends KeelEventLogImpl {

        public InstantLog(@Nonnull String topic, @Nullable JsonArray stack) {
            super(KeelLogLevel.INFO, topic);
            this.toJsonObject().put("stack", stack);
        }
    }
}
