package io.github.sinri.keel.logger.issue.record;

import io.github.sinri.keel.logger.KeelLogLevel;
import io.github.sinri.keel.logger.issue.core.KeelIssueRecord;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BaseIssueRecord implements KeelIssueRecord {
    private final @Nonnull JsonObject attributes;
    private final @Nonnull List<String> classification;
    private long timestamp;
    private @Nonnull KeelLogLevel level;
    private @Nullable Throwable exception;

    public BaseIssueRecord() {
        this.timestamp = System.currentTimeMillis();
        this.attributes = new JsonObject();
        this.level = KeelLogLevel.INFO;
        this.classification = new ArrayList<>();
    }

    @Override
    public void timestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public long timestamp() {
        return timestamp;
    }

    @Override
    public void level(@Nonnull KeelLogLevel level) {
        this.level = level;
    }

    @Nonnull
    @Override
    public KeelLogLevel level() {
        return level;
    }

    @Override
    public void classification(@Nonnull List<String> classification) {
        this.classification.clear();
        this.classification.addAll(classification);
    }

    @Nonnull
    @Override
    public List<String> classification() {
        return classification;
    }

    @Override
    public void attribute(@Nonnull String name, @Nullable Object value) {
        attributes.put(name, value);
    }

    @Nullable
    @Override
    public Object attribute(@Nonnull String name) {
        return attributes.getValue(name);
    }

    @Nonnull
    @Override
    public JsonObject attributes() {
        return attributes;
    }

    @Override
    public void exception(@Nonnull Throwable throwable) {
        this.exception = throwable;
    }

    @Nullable
    @Override
    public Throwable exception() {
        return exception;
    }
}
