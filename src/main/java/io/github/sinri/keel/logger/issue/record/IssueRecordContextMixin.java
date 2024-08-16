package io.github.sinri.keel.logger.issue.record;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @since 3.2.0
 */
public interface IssueRecordContextMixin<T> extends KeelIssueRecordCore<T> {
    String AttributeContext = "context";

    T context(@NotNull JsonObject context);

    default T context(@NotNull Handler<JsonObject> contextHandler) {
        JsonObject context = new JsonObject();
        contextHandler.handle(context);
        return context(context);
    }

    default T context(@NotNull String name, @Nullable Object item) {
        var context = attributes().readJsonObject(AttributeContext);
        if (context == null) {
            context = new JsonObject();
            context(context);
        }
        context.put(name, item);
        return getImplementation();
    }
}
