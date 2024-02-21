package io.github.sinri.keel.logger.issue.core;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface KeelIssueRecorderAdapter<R> extends KeelIssueRecordRender<R> {
    void record(@Nonnull String topic, @Nullable KeelIssueRecord issueRecord);


    void close(@Nonnull Promise<Void> promise);


    @Nonnull
    default Future<Void> gracefullyClose() {
        Promise<Void> voidPromise = Promise.promise();
        close(voidPromise);
        return voidPromise.future();
    }
}
