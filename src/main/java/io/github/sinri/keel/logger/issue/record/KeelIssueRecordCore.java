package io.github.sinri.keel.logger.issue.record;

import io.github.sinri.keel.core.SelfInterface;
import io.github.sinri.keel.core.json.UnmodifiableJsonifiableEntity;
import io.github.sinri.keel.logger.KeelLogLevel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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

    @Nonnull
    String topic();

    T timestamp(long timestamp);

    long timestamp();

    @Nonnull
    UnmodifiableJsonifiableEntity attributes();

    T exception(@Nonnull Throwable throwable);

    @Nullable
    Throwable exception();

    T classification(@Nonnull List<String> classification);

    default T classification(@Nonnull String... classification) {
        List<String> list = new ArrayList<>();
        Collections.addAll(list, classification);
        return classification(list);
    }

    @Nonnull
    List<String> classification();

    T level(@Nonnull KeelLogLevel level);

    @Nonnull
    KeelLogLevel level();
}
