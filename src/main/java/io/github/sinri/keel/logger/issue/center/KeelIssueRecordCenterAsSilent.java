package io.github.sinri.keel.logger.issue.center;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.recorder.adapter.KeelIssueRecorderAdapter;
import io.github.sinri.keel.logger.issue.recorder.adapter.SilentAdapter;
import org.jetbrains.annotations.NotNull;


/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public class KeelIssueRecordCenterAsSilent implements KeelIssueRecordCenter {
    private static final KeelIssueRecordCenterAsSilent instance = new KeelIssueRecordCenterAsSilent();

    private KeelIssueRecordCenterAsSilent() {

    }

    public static KeelIssueRecordCenterAsSilent getInstance() {
        return instance;
    }

    @NotNull
    @Override
    public KeelIssueRecorderAdapter getAdapter() {
        return SilentAdapter.getInstance();
    }
}
