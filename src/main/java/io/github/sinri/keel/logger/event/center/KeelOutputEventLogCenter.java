package io.github.sinri.keel.logger.event.center;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.adapter.OutputAdapter;
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
        return logCenter.createLogger("INSTANT", eventLog -> {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            if (stackTrace.length <= 5) {
                eventLog.put("stack", null);
            } else {
                JsonArray array = new JsonArray();

                var slice = Arrays.copyOfRange(stackTrace, 5, stackTrace.length);
                KeelHelpers.jsonHelper().filterStackTrace(
                        slice,
                        mixedStackPrefixSet,
                        (currentPrefix, ps) -> array.add(currentPrefix + " × " + ps),
                        stackTraceElement -> array.add(stackTraceElement.toString())
                );

                eventLog.put("stack", array);
            }
        });
    }

//    public static void main(String[] args) {
//        KeelOutputEventLogCenter.instantLogger().info("main");
//
//        new P1().a();
//    }
//
//    private static class P1 {
//        void a() {
//            KeelOutputEventLogCenter.instantLogger().info("p1::a");
//        }
//    }
}
