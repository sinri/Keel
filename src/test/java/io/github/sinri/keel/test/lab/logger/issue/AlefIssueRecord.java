package io.github.sinri.keel.test.lab.logger.issue;

import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;

import javax.annotation.Nonnull;

public class AlefIssueRecord extends BaseIssueRecord<AlefIssueRecord> {

    @Nonnull
    @Override
    public AlefIssueRecord getImplementation() {
        return this;
    }

    @Nonnull
    @Override
    public String topic() {
        return "alef";
    }
}
