package io.github.sinri.keel.logger.issue.recorder.adapter;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.render.KeelIssueRecordStringRender;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public class SyncStdoutAdapter implements KeelIssueRecorderAdapter<String>, KeelIssueRecordStringRender {
    private static final SyncStdoutAdapter instance = new SyncStdoutAdapter();

    private SyncStdoutAdapter() {

    }

    public static SyncStdoutAdapter getInstance() {
        return instance;
    }

    @Override
    public void record(@Nonnull String topic, @Nullable KeelIssueRecord<?> issueRecord) {
        if (issueRecord != null) {
            String s = this.renderIssueRecord(issueRecord);
            System.out.println(s);
        }
    }

    @Override
    public void close(@Nonnull Promise<Void> promise) {
        promise.complete();
    }
}
