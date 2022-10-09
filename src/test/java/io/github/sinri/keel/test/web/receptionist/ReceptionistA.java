package io.github.sinri.keel.test.web.receptionist;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.web.legacy.KeelWebRequestReceptionist;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class ReceptionistA extends KeelWebRequestReceptionist {
    public ReceptionistA(RoutingContext routingContext) {
        super(routingContext);
        System.out.println("ReceptionistA::construct");
    }

    @Override
    protected KeelLogger prepareLogger() {
        return Keel.outputLogger("ReceptionistA");
    }

    @Override
    protected Future<Object> dealWithRequest() {
        System.out.println("ReceptionistA::dealWithRequest");
        JsonObject bodyAsJson = getRoutingContext().getBodyAsJson();
        if (bodyAsJson == null) {
            System.out.println("ReceptionistA::dealWithRequest bodyAsJson is null");
            return Future.failedFuture(new NullPointerException("body as json is null"));
        }
        var a = bodyAsJson.getString("a");
        System.out.println("ReceptionistA read a: " + a);
        return Future.succeededFuture();
    }
}
