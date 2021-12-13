package io.github.sinri.Keel.test.web.controller;

import io.github.sinri.Keel.web.KeelWebRequestController;
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
