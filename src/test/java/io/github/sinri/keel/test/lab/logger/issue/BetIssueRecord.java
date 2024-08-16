package io.github.sinri.keel.test.lab.logger.issue;

import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;
import org.jetbrains.annotations.NotNull;


public final class BetIssueRecord extends BaseIssueRecord<BetIssueRecord> {
    public BetIssueRecord(@NotNull String caller) {
        this.classification("Bet", caller);
    }

    @Override
    public @NotNull BetIssueRecord getImplementation() {
        return this;
    }


    @Override
    public @NotNull String topic() {
        return "Bet";
    }

    public BetIssueRecord setData(int x) {
        this.attribute("data", x);
        return this;
    }
}
