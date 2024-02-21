package io.github.sinri.keel.test.lab.logger.issue;

import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenterAsSync;
import io.github.sinri.keel.logger.issue.core.KeelIssueRecorder;
import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.adapter.SyncStdoutAdapter;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.List;

public class IssueLoggerTest extends KeelTest {
    @Nonnull
    @Override
    protected Future<Void> starting() {
        return super.starting();
    }

    @TestUnit
    public Future<Void> testForStdOutSync() {
        KeelIssueRecordCenterAsSync<String> center = new KeelIssueRecordCenterAsSync<>(SyncStdoutAdapter.getInstance());
        KeelIssueRecorder<BaseIssueRecord, String> recorder = center.generateRecorder("StandoutOutputSync", BaseIssueRecord::new);
        recorder.record(issue -> {
            issue.classification(List.of("IssueLoggerTest", "testForStdOutSync"));
        });
        return Future.succeededFuture();
    }
}
