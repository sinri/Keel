package io.github.sinri.keel.test.lab.logger.issue;

import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;

import javax.annotation.Nonnull;

public final class BetIssueRecord extends BaseIssueRecord<BetIssueRecord> {
    public BetIssueRecord(@Nonnull String caller) {
        this.classification("Bet", caller);
    }

    @Nonnull
    @Override
    public BetIssueRecord getImplementation() {
        return this;
    }

    @Nonnull
    @Override
    public String topic() {
        return "Bet";
    }

    public BetIssueRecord setData(int x) {
        this.attribute("data", x);
        return this;
    }
}
