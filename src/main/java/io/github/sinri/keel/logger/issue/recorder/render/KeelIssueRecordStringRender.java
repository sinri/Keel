package io.github.sinri.keel.logger.issue.recorder.render;

import io.github.sinri.keel.helper.KeelDateTimeHelper;
import io.github.sinri.keel.logger.issue.core.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.core.KeelIssueRecordRender;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

public interface KeelIssueRecordStringRender extends KeelIssueRecordRender<String> {
    @Nonnull
    @Override
    default String renderIssueRecord(@Nonnull KeelIssueRecord issueRecord) {
        String s = "㏒ " + KeelHelpers.datetimeHelper().getDateExpression(issueRecord.timestamp(), KeelDateTimeHelper.MYSQL_DATETIME_MS_PATTERN)
                + " [" + issueRecord.level().name() + "]"
                + " " + KeelHelpers.stringHelper().joinStringArray(issueRecord.classification(), "::");
        if (!issueRecord.attributes().isEmpty()) {
            s += "\n" + issueRecord.attributes();
        }
        Throwable exception = issueRecord.exception();
        if (exception != null) {
            s += "\nException Thrown:\n" + renderThrowable(exception);
        }
        return s;
    }

    @Nonnull
    @Override
    default String renderThrowable(@Nonnull Throwable throwable) {
        return KeelHelpers.stringHelper().renderThrowableChain(throwable, ignorableStackPackageSet());
    }
}
