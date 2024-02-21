package io.github.sinri.keel.logger.issue.core;

import io.github.sinri.keel.helper.KeelRuntimeHelper;

import javax.annotation.Nonnull;
import java.util.Set;

public interface KeelIssueRecordRender<R> {
    @Nonnull
    R renderIssueRecord(@Nonnull KeelIssueRecord issueRecord);

    @Nonnull
    R renderThrowable(@Nonnull Throwable throwable);

    @Nonnull
    default Set<String> ignorableStackPackageSet() {
        return KeelRuntimeHelper.ignorableCallStackPackage;
    }
}
