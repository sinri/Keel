package io.github.sinri.keel.logger.issue.record.event;

import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;
import io.github.sinri.keel.logger.issue.record.IssueRecordContextMixin;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

public abstract class RoutineBaseIssueRecord<T> extends BaseIssueRecord<T> implements IssueRecordContextMixin<T> {

    private final @Nonnull String topic;

    public RoutineBaseIssueRecord(@Nonnull String topic) {
        this.topic = topic;
    }

    @Override
    public T context(@Nonnull JsonObject context) {
        this.attribute(AttributeContext, context);
        return getImplementation();
    }

    @Nonnull
    @Override
    public final String topic() {
        return topic;
    }
}
