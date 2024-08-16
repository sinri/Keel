package io.github.sinri.keel.logger.issue.recorder.render;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.helper.KeelDateTimeHelper;
import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import org.jetbrains.annotations.NotNull;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 3.1.10
 */
@TechnicalPreview(since = "3.1.10")
public class KeelIssueRecordStringRender implements KeelIssueRecordRender<String> {
    private static final KeelIssueRecordStringRender instance = new KeelIssueRecordStringRender();

    protected KeelIssueRecordStringRender() {

    }

    public static KeelIssueRecordStringRender getInstance() {
        return instance;
    }

    @NotNull
    @Override
    public String renderIssueRecord(@NotNull KeelIssueRecord<?> issueRecord) {
        StringBuilder s = new StringBuilder("㏒ ");
        s.append(KeelHelpers.datetimeHelper().getDateExpression(issueRecord.timestamp(), KeelDateTimeHelper.MYSQL_DATETIME_MS_PATTERN));
        s.append(" [").append(issueRecord.level().name()).append("]");
        s.append(" ").append(issueRecord.topic()).append(" (").append(KeelHelpers.stringHelper().joinStringArray(issueRecord.classification(), ",")).append(")");
        if (!issueRecord.attributes().isEmpty()) {
            issueRecord.attributes().forEach(attribute -> {
                s.append("\n ▪ ").append(attribute.getKey()).append(": ").append(attribute.getValue());
            });
        }
        Throwable exception = issueRecord.exception();
        if (exception != null) {
            s.append("\n ⊹ Exception Thrown:\n").append(renderThrowable(exception));
        }
        return s.toString();
    }

    @NotNull
    @Override
    public String renderThrowable(@NotNull Throwable throwable) {
        return KeelHelpers.stringHelper().renderThrowableChain(throwable, ignorableStackPackageSet());
    }
}
