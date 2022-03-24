package io.github.sinri.keel.test.web.controller;

import io.github.sinri.keel.web.KeelApiAnnotation;
import io.github.sinri.keel.web.KeelWebRequestController;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

public class UrlRuleController extends KeelWebRequestController {
    public UrlRuleController(RoutingContext ctx) {
        super(ctx);
    }

    @KeelApiAnnotation(urlRule = "/UrlRuleController/demo1")
    public Future<String> demo1() {
        return Future.succeededFuture(ctx.request().absoluteURI());
    }
}
