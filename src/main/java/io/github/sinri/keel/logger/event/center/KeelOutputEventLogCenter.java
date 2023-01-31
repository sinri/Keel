package io.github.sinri.keel.logger.event.center;

import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.adapter.OutputAdapter;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;

import java.util.Set;
import java.util.function.Function;

/**
 * @since 3.0.0
 */
public class KeelOutputEventLogCenter extends KeelSyncEventLogCenter {
    private final static KeelOutputEventLogCenter defaultInstance = new KeelOutputEventLogCenter(null);
    private static Set<String> mixedStackPrefixSet = Set.of(
            "io.vertx.",
            "java.",
            "io.netty."
    );

    private KeelOutputEventLogCenter(Function<KeelEventLog, Future<String>> converter) {
        super(OutputAdapter.getInstance(converter));
    }

    public static KeelOutputEventLogCenter getInstance() {
        return defaultInstance;
    }

    public static KeelOutputEventLogCenter getInstance(Function<KeelEventLog, Future<String>> converter) {
        return new KeelOutputEventLogCenter(converter);
    }

    /**
     * @param mixedStackPrefixSet name prefix of class to ignore
     * @since 3.0.0
     */
    public static void setMixedStackPrefixSet(Set<String> mixedStackPrefixSet) {
        KeelOutputEventLogCenter.mixedStackPrefixSet = mixedStackPrefixSet;
    }

    public static KeelEventLogger instantLogger() {
        var logCenter = getInstance(KeelEventLog::render);
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length == 0) {
            return logCenter.createLogger("INSTANT", eventLog -> eventLog.put("stack", null));
        } else {
            JsonArray array = new JsonArray();

            KeelHelpers.jsonHelper().filterStackTrace(
                    stackTrace,
                    mixedStackPrefixSet,
                    (currentPrefix, ps) -> array.add(currentPrefix + " × " + ps),
                    stackTraceElement -> array.add(stackTraceElement.toString())
            );

            return logCenter.createLogger("INSTANT", eventLog -> eventLog.put("stack", array));
        }
    }
}
