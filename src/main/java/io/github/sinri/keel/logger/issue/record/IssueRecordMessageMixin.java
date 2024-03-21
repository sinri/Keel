package io.github.sinri.keel.logger.issue.record;

import io.github.sinri.keel.core.TechnicalPreview;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public interface IssueRecordMessageMixin<T> extends KeelIssueRecordCore<T> {
    String AttributeMessage = "message";

    T message(@Nonnull String message);

    @Nullable
    String message();
}
