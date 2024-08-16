package io.github.sinri.keel.logger.issue.recorder;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.vertx.core.Handler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

/**
 * @param <T> The type of the certain implementation of the issue record used.
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public interface KeelIssueRecorder<T extends KeelIssueRecord<?>> {

    static <T extends KeelIssueRecord<?>> KeelIssueRecorder<T> build(
            @NotNull KeelIssueRecordCenter issueRecordCenter,
            @NotNull Supplier<T> issueRecordBuilder,
            @NotNull String topic
    ) {
        return new KeelIssueRecorderImpl<T>(issueRecordCenter, issueRecordBuilder, topic);
    }

    @NotNull
    KeelLogLevel getVisibleLevel();

    void setVisibleLevel(@NotNull KeelLogLevel level);

    @NotNull
    KeelIssueRecordCenter issueRecordCenter();

    /**
     * @return an instance of issue, to be modified for details.
     */
    @NotNull
    Supplier<T> issueRecordBuilder();

    /**
     * @since 3.2.0
     */
    void addBypassIssueRecorder(@NotNull KeelIssueRecorder<T> bypassIssueRecorder);

    /**
     * @since 3.2.0
     */
    @NotNull
    List<KeelIssueRecorder<T>> getBypassIssueRecorders();

    @NotNull
    String topic();

    @Nullable
    Handler<T> getRecordFormatter();

    void setRecordFormatter(@Nullable Handler<T> handler);

    /**
     * Record an issue (created with `issueRecordBuilder` and modified with `issueHandler`).
     * It may be handled later async, actually.
     *
     * @param issueHandler the handler to modify the base issue.
     */
    default void record(@NotNull Handler<T> issueHandler) {
        T issue = this.issueRecordBuilder().get();
        issueHandler.handle(issue);

        Handler<T> recordFormatter = getRecordFormatter();
        if (recordFormatter != null) {
            recordFormatter.handle(issue);
        }

        if (issue.level().isEnoughSeriousAs(getVisibleLevel())) {
            this.issueRecordCenter().getAdapter().record(topic(), issue);
        }

        getBypassIssueRecorders().forEach(keelIssueRecorder -> {
            if (issue.level().isEnoughSeriousAs(keelIssueRecorder.getVisibleLevel())) {
                keelIssueRecorder.issueRecordCenter().getAdapter().record(topic(), issue);
            }
        });
    }

    default void debug(@NotNull Handler<T> issueHandler) {
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.DEBUG);
        });
    }

    default void info(@NotNull Handler<T> issueHandler) {
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.INFO);
        });
    }

    default void notice(@NotNull Handler<T> issueHandler) {
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.NOTICE);
        });
    }

    default void warning(@NotNull Handler<T> issueHandler) {
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.WARNING);
        });
    }

    default void error(@NotNull Handler<T> issueHandler) {
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.ERROR);
        });
    }

    default void fatal(@NotNull Handler<T> issueHandler) {
        record(t -> {
            issueHandler.handle(t);
            t.level(KeelLogLevel.FATAL);
        });
    }

    default void exception(@NotNull Throwable throwable, @NotNull Handler<T> issueHandler) {
        error(t -> {
            t.exception(throwable);
            issueHandler.handle(t);
        });
    }

    default void exception(@NotNull Throwable throwable, @NotNull String message) {
        exception(throwable, t -> t.message(message));
    }

    default void exception(@NotNull Throwable throwable) {
        exception(throwable, t -> {
        });
    }

    default void debug(@NotNull String message) {
        debug(t -> t.message(message));
    }

    default void info(@NotNull String message) {
        info(t -> t.message(message));
    }

    default void notice(@NotNull String message) {
        notice(t -> t.message(message));
    }

    default void warning(@NotNull String message) {
        warning(t -> t.message(message));
    }

    default void error(@NotNull String message) {
        error(t -> t.message(message));
    }

    default void fatal(@NotNull String message) {
        fatal(t -> t.message(message));
    }

}
