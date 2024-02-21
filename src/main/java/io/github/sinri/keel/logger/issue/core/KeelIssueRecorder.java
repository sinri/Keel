package io.github.sinri.keel.logger.issue.core;

import io.github.sinri.keel.core.TechnicalPreview;
import io.vertx.core.Handler;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * @param <T> The type of the certain implementation of the issue record used.
 * @param <R> The type that the issue record would be rendered to.
 */
@TechnicalPreview(since = "3.1.10")
public interface KeelIssueRecorder<T extends KeelIssueRecord, R> {
    @Nonnull
    KeelIssueRecordCenter<R> issueRecordCenter();

    /**
     * @return an instance of issue, to be modified for details.
     */
    @Nonnull
    Supplier<T> issueRecordBuilder();

    @Nonnull
    String topic();

    /**
     * Record an issue (created with `issueRecordBuilder` and modified with `issueHandler`).
     * It may be handled later async, actually.
     *
     * @param issueHandler the handler to modify the base issue.
     */
    default void record(@Nonnull Handler<T> issueHandler) {
        T issue = this.issueRecordBuilder().get();
        issueHandler.handle(issue);
        this.issueRecordCenter().getAdapter().record(topic(), issue);
    }
}
