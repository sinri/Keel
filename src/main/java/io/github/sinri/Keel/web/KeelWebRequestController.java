package io.github.sinri.Keel.web;

import io.github.sinri.Keel.core.logger.KeelLogger;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class KeelWebRequestController {
    protected RoutingContext ctx;
    protected KeelLogger logger;

    public KeelWebRequestController(RoutingContext ctx) {
        this.ctx = ctx;
        this.logger = new KeelLogger(KeelLogger.class.getName());

        System.out.println("do something before call method!");
    }

    protected Future<Void> sayOK() {
        return sayOK(null);
    }

    protected Future<Void> sayOK(Object data) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("code", "OK");
        if (data == null) {
            jsonObject.putNull("data");
        } else {
            jsonObject.put("data", data);
        }
        return ctx.json(jsonObject);
    }

    protected Future<Void> sayFail(Object error) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("code", "FAILED");
        if (error == null) {
            jsonObject.putNull("data");
        } else {
            jsonObject.put("data", error);
        }
        return ctx.json(jsonObject);
    }
}
