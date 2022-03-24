package io.github.sinri.keel.test.web.controller;

import io.github.sinri.keel.web.KeelApiAnnotation;
import io.github.sinri.keel.web.KeelWebRequestController;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class SampleController extends KeelWebRequestController {
    public SampleController(RoutingContext ctx) {
        super(ctx);
    }

//    public void SampleMethod(String p1) {
//        System.out.println("now call method!");
//        sayOK(new JsonObject().put("p1", p1));
//    }

    public Future<JsonObject> ReturnJsonObjectMethod(String p1) {
        if (p1.equals("ERROR")) {
            // throw new Exception("java.lang.reflect.InvocationTargetException: "+p1);
            return Future.failedFuture("failed " + p1);
        }
        return Future.succeededFuture(new JsonObject().put("p1", p1));
    }

    @KeelApiAnnotation(responseContentType = "text/plain", acceptedRequestMethods = {"POST"})
    public Future<String> ReturnTextMethod(String p1) {
        if (p1.equals("ERROR")) {
            throw new RuntimeException("RuntimeException: " + p1);
//            return Future.failedFuture("failed "+p1);
        }
        return Future.succeededFuture("p1=" + p1);
    }
}
