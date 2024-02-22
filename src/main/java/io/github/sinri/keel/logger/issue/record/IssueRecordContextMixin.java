package io.github.sinri.keel.logger.issue.record;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 3.2.0
 */
public interface IssueRecordContextMixin<T> extends KeelIssueRecord<T>, IssueRecordMessageMixin<T> {
    String AttributeContext = "context";

    T context(@Nonnull JsonObject context);

    default T context(@Nonnull Handler<JsonObject> contextHandler) {
        JsonObject context = new JsonObject();
        contextHandler.handle(context);
        return context(context);
    }

    default T context(@Nonnull String name, @Nullable Object item) {
        var context = attributes().readJsonObject(AttributeContext);
        if (context == null) {
            context = new JsonObject();
            context(context);
        }
        context.put(name, item);
        return getImplementation();
    }
}
