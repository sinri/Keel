package io.github.sinri.keel.web.legacy;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @since 1.1
 * @since 1.10 became abstract
 * @since 2.8.1 moved here
 */
@Deprecated
public abstract class KeelWebRequestFilter {
    protected Method method;

    /**
     * @since 2.4
     */
    protected static List<String> getClientIPChain(RoutingContext ctx) {
        JsonArray array = ctx.get(KeelWebRequestReceptionist.RoutingContextDatumKeyOfClientIPChain);
        List<String> list = new ArrayList<>();
        if (array != null) {
            array.forEach(item -> {
                list.add(item.toString());
            });
        }
        return list;
    }

    public String filterName() {
        return getClass().getName();
    }

    public KeelWebRequestFilter setTargetMethod(Method method) {
        this.method = method;
        return this;
    }

    abstract public Future<Void> shouldHandleThisRequest(RoutingContext ctx);
}
