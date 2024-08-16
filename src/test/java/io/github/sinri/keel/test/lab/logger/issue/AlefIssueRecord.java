package io.github.sinri.keel.test.lab.logger.issue;

import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;
import org.jetbrains.annotations.NotNull;



public class AlefIssueRecord extends BaseIssueRecord<AlefIssueRecord> {


    @Override
    public @NotNull AlefIssueRecord getImplementation() {
        return this;
    }


    @Override
    public @NotNull String topic() {
        return "alef";
    }
}
