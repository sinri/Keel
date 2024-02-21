package io.github.sinri.keel.logger.issue.recorder.render;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.helper.KeelRuntimeHelper;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
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

    @Nonnull
    R renderIssueRecord(@Nonnull KeelIssueRecord<?> issueRecord);

    @Nonnull
    R renderThrowable(@Nonnull Throwable throwable);

    @Nonnull
    default Set<String> ignorableStackPackageSet() {
        return KeelRuntimeHelper.ignorableCallStackPackage;
    }
}
