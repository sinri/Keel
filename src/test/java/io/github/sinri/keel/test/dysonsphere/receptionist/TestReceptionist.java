package io.github.sinri.keel.test.dysonsphere.receptionist;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.web.ApiMeta;
import io.github.sinri.keel.web.http.receptionist.KeelWebFutureReceptionist;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@ApiMeta(routePath = "/receptionist/test", allowMethods = {"GET", "POST"})
public class TestReceptionist extends KeelWebFutureReceptionist {
    public TestReceptionist(Keel keel, RoutingContext routingContext) {
        super(keel, routingContext);
    }

    @Override
    protected Future<Object> handleForFuture() {
        JsonObject jsonObject = new JsonObject().put("path", getRoutingContext().request().path());
        return Future.succeededFuture(jsonObject);
    }

    @Override
    protected KeelEventLogger createLogger() {
        return getKeel().createOutputEventLogger(getClass().getName());
    }
}
