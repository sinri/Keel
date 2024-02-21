package io.github.sinri.keel.test.lab.logger.issue;

import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenterAsSync;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.logger.issue.recorder.adapter.SyncStdoutAdapter;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.List;

public class IssueStdOutLoggerTest extends KeelTest {
    private KeelIssueRecordCenterAsSync center;

    @Nonnull
    @Override
    protected Future<Void> starting() {
        center = new KeelIssueRecordCenterAsSync(SyncStdoutAdapter.getInstance());

        return Future.succeededFuture();
    }

    @TestUnit
    public Future<Void> testForStdOutSyncAlef() {
        KeelIssueRecorder<AlefIssueRecord> recorder = center.generateRecorder("StandoutOutputSync", AlefIssueRecord::new);
        recorder.record(issue -> {
            issue.classification(List.of("IssueLoggerTest", "testForStdOutSync"));
        });
        return Future.succeededFuture();
    }

    @TestUnit
    public Future<Void> testForStdOutSyncBet() {
        KeelIssueRecorder<BetIssueRecord> recorder = center.generateRecorder("StandoutOutputSync", () -> {
            return new BetIssueRecord("testForStdOutSyncBet");
        });
        recorder.warning(t -> {
            t.message("Who is the boss?").setData(1);
        });

        recorder.exception(new NullPointerException("TEST"), t -> t.setData(999));

        return Future.succeededFuture();
    }
}
