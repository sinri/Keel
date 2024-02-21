package io.github.sinri.keel.logger.issue.core;

public interface KeelIssueRecorderAdapterAsync<R> extends KeelIssueRecorderAdapter<R> {
    boolean isStopped();

    boolean isClosed();
}
