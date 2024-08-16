package io.github.sinri.keel.logger.issue.center;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import io.github.sinri.keel.logger.issue.recorder.adapter.SyncStdoutAdapter;
import org.jetbrains.annotations.NotNull;



/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public class KeelIssueRecordCenterAsSync implements KeelIssueRecordCenter {
    protected final @NotNull KeelIssueRecorderAdapter adapter;

    public KeelIssueRecordCenterAsSync(@NotNull KeelIssueRecorderAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public @NotNull KeelIssueRecorderAdapter getAdapter() {
        return adapter;
    }

    /**
     * @since 3.2.7
     */
    private static final KeelIssueRecordCenterAsSync outputCenter = new KeelIssueRecordCenterAsSync(SyncStdoutAdapter.getInstance());

    /**
     * @since 3.2.7
     */
    public static KeelIssueRecordCenter getInstanceWithStdout() {
        return outputCenter;
    }
}
