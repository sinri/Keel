package io.github.sinri.keel.logger.issue.recorder.render;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.helper.KeelRuntimeHelper;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public interface KeelIssueRecordRender<R> {

    static KeelIssueRecordRender<String> renderForString() {
        return KeelIssueRecordStringRender.getInstance();
    }

    static KeelIssueRecordRender<JsonObject> renderForJsonObject() {
        return KeelIssueRecordJsonObjectRender.getInstance();
    }

    @NotNull
    R renderIssueRecord(@NotNull KeelIssueRecord<?> issueRecord);

    @NotNull
    R renderThrowable(@NotNull Throwable throwable);

    @NotNull
    default Set<String> ignorableStackPackageSet() {
        return KeelRuntimeHelper.ignorableCallStackPackage;
    }
}
