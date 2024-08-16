package io.github.sinri.keel.logger.issue.recorder.adapter;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.render.KeelIssueRecordRender;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public interface KeelIssueRecorderAdapter {
    KeelIssueRecordRender<?> issueRecordRender();

    void record(@NotNull String topic, @Nullable KeelIssueRecord<?> issueRecord);


    void close(@NotNull Promise<Void> promise);


    @NotNull
    default Future<Void> gracefullyClose() {
        Promise<Void> voidPromise = Promise.promise();
        close(voidPromise);
        return voidPromise.future();
    }

    boolean isStopped();

    boolean isClosed();
}
