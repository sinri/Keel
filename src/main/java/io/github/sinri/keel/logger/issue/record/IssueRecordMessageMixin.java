package io.github.sinri.keel.logger.issue.record;

import io.github.sinri.keel.core.TechnicalPreview;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public interface IssueRecordMessageMixin<T> extends KeelIssueRecordCore<T> {
    String AttributeMessage = "message";

    T message(@NotNull String message);

    @Nullable
    String message();
}
