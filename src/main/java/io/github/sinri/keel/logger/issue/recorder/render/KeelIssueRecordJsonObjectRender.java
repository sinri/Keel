package io.github.sinri.keel.logger.issue.recorder.render;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import java.util.Objects;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public interface KeelIssueRecordJsonObjectRender extends KeelIssueRecordRender<JsonObject> {
    @Nonnull
    @Override
    default JsonObject renderIssueRecord(@Nonnull KeelIssueRecord<?> issueRecord) {
        // by default, the topic is neglected for Aliyun SLS eco

        JsonObject x = new JsonObject();

        x.put("level", issueRecord.level());

        JsonArray classification = new JsonArray();
        issueRecord.classification().forEach(classification::add);
        x.put("classification", classification);

        issueRecord.attributes().forEach(entry -> {
            x.put(entry.getKey(), entry.getValue());
        });
        Throwable exception = issueRecord.exception();
        if (exception != null) {
            x.put("exception", renderThrowable(exception));
        }
        return x;
    }

    @Nonnull
    @Override
    default JsonObject renderThrowable(@Nonnull Throwable throwable) {
        return Objects.requireNonNull(KeelHelpers.jsonHelper().renderThrowableChain(throwable, ignorableStackPackageSet()));
    }
}
