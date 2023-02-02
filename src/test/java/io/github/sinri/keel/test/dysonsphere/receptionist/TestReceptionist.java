package io.github.sinri.keel.test.dysonsphere.receptionist;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.web.http.ApiMeta;
import io.github.sinri.keel.web.http.receptionist.KeelWebFutureReceptionist;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@ApiMeta(routePath = "/receptionist/test", allowMethods = {"GET", "POST"})
public class TestReceptionist extends KeelWebFutureReceptionist {
    public TestReceptionist(RoutingContext routingContext) {
        super(routingContext);
    }

    @Override
    protected Future<Object> handleForFuture() {
        getLogger().info("handleForFuture start");
        JsonObject jsonObject = new JsonObject().put("path", getRoutingContext().request().path());
        getLogger().info("handleForFuture ready");
        return Future.succeededFuture(jsonObject);
    }

    @Override
    protected KeelEventLogger createLogger() {
        return KeelOutputEventLogCenter.getInstance().createLogger(getClass().getName());
    }
}
