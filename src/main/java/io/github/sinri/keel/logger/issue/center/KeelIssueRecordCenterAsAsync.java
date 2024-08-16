package io.github.sinri.keel.logger.issue.center;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import org.jetbrains.annotations.NotNull;


/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public class KeelIssueRecordCenterAsAsync implements KeelIssueRecordCenter {
    protected final @NotNull KeelIssueRecorderAdapter adapter;

    public KeelIssueRecordCenterAsAsync(@NotNull KeelIssueRecorderAdapter adapter) {
        this.adapter = adapter;
    }


    @Override
    public @NotNull KeelIssueRecorderAdapter getAdapter() {
        return adapter;
    }
}
