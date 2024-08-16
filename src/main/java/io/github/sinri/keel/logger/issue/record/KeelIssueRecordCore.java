package io.github.sinri.keel.logger.issue.record;

import io.github.sinri.keel.core.SelfInterface;
import io.github.sinri.keel.core.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.logger.KeelLogLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @since 3.2.0
 */
public interface KeelIssueRecordCore<T> extends SelfInterface<T> {
    String AttributeClassification = "classification";
    String AttributeLevel = "level";
    String AttributeException = "exception";

    @NotNull
    String topic();

    T timestamp(long timestamp);

    long timestamp();

    @NotNull
    UnmodifiableJsonifiableEntity attributes();

    T exception(@NotNull Throwable throwable);

    @Nullable
    Throwable exception();

    T classification(@NotNull List<String> classification);

    default T classification(@NotNull String... classification) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, classification);
        return classification(list);
    }

    @NotNull
    List<String> classification();

    T level(@NotNull KeelLogLevel level);

    @NotNull
    KeelLogLevel level();
}
