package io.github.sinri.keel.verticles;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.jetbrains.annotations.NotNull;


abstract public class KeelVerticleImplWithIssueRecorder<T extends KeelIssueRecord<T>> extends AbstractVerticle implements KeelVerticle {
    private @NotNull KeelIssueRecorder<T> issueRecorder;

    public KeelVerticleImplWithIssueRecorder() {
        this.issueRecorder = buildIssueRecorder();
    }

    @NotNull
    public KeelIssueRecorder<T> getIssueRecorder() {
        return issueRecorder;
    }

    abstract protected @NotNull KeelIssueRecorder<T> buildIssueRecorder();

    @Override
    public final void start(Promise<Void> startPromise) {
        this.issueRecorder = buildIssueRecorder();
        startAsKeelVerticle(startPromise);
    }

    @Override
    public final void start() {
        this.issueRecorder = buildIssueRecorder();
        this.startAsKeelVerticle();
    }

    protected void startAsKeelVerticle(Promise<Void> startPromise) {
        startAsKeelVerticle();
        startPromise.complete();
    }

    abstract protected void startAsKeelVerticle();
}
