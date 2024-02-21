package io.github.sinri.keel.logger.issue.recorder.adapter;

import io.github.sinri.keel.core.TechnicalPreview;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public interface KeelIssueRecorderAdapterAsync<R> extends KeelIssueRecorderAdapter<R> {
    boolean isStopped();

    boolean isClosed();
}
