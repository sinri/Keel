package io.github.sinri.keel.logger.issue.recorder.adapter;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.render.KeelIssueRecordRender;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public final class SilentAdapter implements KeelIssueRecorderAdapter {
    private static final SilentAdapter instance = new SilentAdapter();

    private SilentAdapter() {
    }

    public static SilentAdapter getInstance() {
        return instance;
    }

    @Override
    public KeelIssueRecordRender<?> issueRecordRender() {
        return KeelIssueRecordRender.renderForString();
    }

    @Override
    public void record(@Nonnull String topic, @Nullable KeelIssueRecord<?> issueRecord) {
        // do nothing
    }

    @Override
    public void close(@Nonnull Promise<Void> promise) {
        promise.complete();
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public boolean isClosed() {
        return false;
    }
}
