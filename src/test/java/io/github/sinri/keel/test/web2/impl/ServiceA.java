package io.github.sinri.keel.test.web2.impl;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.test.web2.Service;
import io.github.sinri.keel.web.ApiMeta;
import io.github.sinri.keel.web.handler.KeelPlatformHandler;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

@ApiMeta(routePath = "/a", timeout = 0)
public class ServiceA extends Service {
    @Override
    protected Future<Object> handleRequestForFuture(RoutingContext routingContext) {
        Keel.outputLogger().info("ServiceA KEEL_REQUEST_ID: " + routingContext.get(KeelPlatformHandler.KEEL_REQUEST_ID));
        return Keel.callFutureSleep(2000L).compose(v -> {
            return Future.succeededFuture("DONE");
        });
    }
}
