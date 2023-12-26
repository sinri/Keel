package io.github.sinri.keel.test.dysonsphere.receptionist;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.web.http.ApiMeta;
import io.github.sinri.keel.web.http.receptionist.KeelWebReceptionist;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

@ApiMeta(routePath = "/receptionist/sse", allowMethods = {"GET", "POST"}, timeout = 0)
public class SSEReceptionist extends KeelWebReceptionist {
    public SSEReceptionist(RoutingContext routingContext) {
        super(routingContext);
    }

    @Override
    public void handle() {
        HttpServerResponse response = getRoutingContext().response();
        response
                .putHeader("Content-Type", "text/event-stream")
                .putHeader("Cache-Control", "no-cache")
                .setChunked(true);

        long timer = Keel.getVertx().setPeriodic(1000L, x -> {
            response.write("event: update\n");
            response.write("data: " + ("Now is " + System.currentTimeMillis()) + "\n\n");
        });

        response.endHandler(v -> {
            Keel.getVertx().cancelTimer(timer);
        });
    }

    @Override
    protected KeelEventLogger createLogger() {
        return KeelOutputEventLogCenter.instantLogger();
    }


}
