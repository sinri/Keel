package io.github.sinri.keel.logger.issue.core;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

@TechnicalPreview(since = "3.1.10")
public interface KeelIssueRecord {

    void timestamp(long timestamp);

    long timestamp();

    void level(@Nonnull KeelLogLevel level);

    @Nonnull
    KeelLogLevel level();

    void classification(@Nonnull List<String> classification);

    @Nonnull
    List<String> classification();

    void attribute(@Nonnull String name, @Nullable Object value);

    @Nullable
    Object attribute(@Nonnull String name);

    @Nonnull
    JsonObject attributes();

    void exception(@Nonnull Throwable throwable);

    @Nullable
    Throwable exception();
}
