package io.github.sinri.keel.logger.issue.record;

import io.github.sinri.keel.core.SelfInterface;
import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public interface KeelIssueRecord<T> extends SelfInterface<T> {
    String AttributeClassification = "classification";

    @Nonnull
    String topic();

    T timestamp(long timestamp);

    long timestamp();

    @Nonnull
    JsonObject attributes();

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
