package io.github.sinri.keel.logger.issue.recorder.adapter;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.render.KeelIssueRecordRender;
import io.github.sinri.keel.servant.intravenous.KeelIntravenous;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public class AsyncStdoutAdapter implements KeelIssueRecorderAdapter {
    private static final AsyncStdoutAdapter instance = new AsyncStdoutAdapter();
    private final KeelIntravenous<KeelIssueRecord<?>> intravenous;
    private volatile boolean stopped = false;
    private volatile boolean closed = true;

    private AsyncStdoutAdapter() {
        this.intravenous = new KeelIntravenous<>(keelIssueRecords -> KeelAsyncKit.iterativelyCall(keelIssueRecords, this::writeOneIssueRecord));
        this.intravenous.start();
        closed = false;
    }

    public static AsyncStdoutAdapter getInstance() {
        return instance;
    }

    private Future<Void> writeOneIssueRecord(@Nonnull KeelIssueRecord<?> issueRecord) {
        String s = this.issueRecordRender().renderIssueRecord(issueRecord);
        System.out.println(s);
        return Future.succeededFuture();
    }

    @Override
    public KeelIssueRecordRender<String> issueRecordRender() {
        return KeelIssueRecordRender.renderForString();
    }

    @Override
    public void record(@Nonnull String topic, @Nullable KeelIssueRecord<?> issueRecord) {
        if (issueRecord != null) {
            this.intravenous.add(issueRecord);
        }
    }

    @Override
    public void close(@Nonnull Promise<Void> promise) {
        this.stopped = true;
        this.intravenous.shutdown()
                .andThen(ar -> {
                    if (ar.failed()) {
                        promise.fail(ar.cause());
                    } else {
                        this.closed = true;
                        promise.complete();
                    }
                });
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }
}
