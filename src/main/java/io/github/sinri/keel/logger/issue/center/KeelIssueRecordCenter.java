package io.github.sinri.keel.logger.issue.center;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import io.vertx.core.Handler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * @since 3.1.10 Technical Preview
 */
@TechnicalPreview(since = "3.1.10")
public interface KeelIssueRecordCenter {
    /**
     * @since 3.2.7 Use a static singleton impl.
     */
    static KeelIssueRecordCenter outputCenter() {
        return KeelIssueRecordCenterAsSync.getInstanceWithStdout();
    }

    static KeelIssueRecordCenter silentCenter() {
        return KeelIssueRecordCenterAsSilent.getInstance();
    }

    static <X extends KeelIssueRecord<?>> KeelIssueRecorder<X> createSilentIssueRecorder() {
        return silentCenter().generateIssueRecorder("Silent", () -> null);
    }

    @NotNull
    KeelIssueRecorderAdapter getAdapter();

    /**
     * @param issueRecordBuilder Sample for silent: {@code Supplier<T> issueRecordBuilder= () -> null;}
     */
    @NotNull
    default <T extends KeelIssueRecord<?>> KeelIssueRecorder<T> generateIssueRecorder(@NotNull String topic, @NotNull Supplier<T> issueRecordBuilder) {
        return KeelIssueRecorder.build(this, issueRecordBuilder, topic);
    }

    @NotNull
    default KeelEventLogger generateEventLogger(@NotNull String topic) {
        return KeelEventLogger.from(generateIssueRecorderForEventLogger(topic));
    }

    @NotNull
    default KeelEventLogger generateEventLogger(@NotNull String topic, @Nullable Handler<KeelEventLog> templateEventLogEditor) {
        return KeelEventLogger.from(generateIssueRecorderForEventLogger(topic), templateEventLogEditor);
    }

    @NotNull
    default KeelIssueRecorder<KeelEventLog> generateIssueRecorderForEventLogger(@NotNull String topic) {
        return generateIssueRecorder(topic, () -> new KeelEventLog(topic));
    }

}
