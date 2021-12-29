package io.github.sinri.keel.test.web.controller;

import io.github.sinri.keel.web.KeelWebRequestController;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class SampleController extends KeelWebRequestController {
    public SampleController(RoutingContext ctx) {
        super(ctx);
    }

    public void SampleMethod(String p1) {
        System.out.println("now call method!");
        sayOK(new JsonObject().put("p1", p1));
    }
}
