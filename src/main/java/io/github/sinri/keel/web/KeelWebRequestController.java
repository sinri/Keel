package io.github.sinri.keel.web;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public class KeelWebRequestController {
    protected final RoutingContext ctx;
    protected KeelLogger logger;

    public KeelWebRequestController(RoutingContext ctx) {
        this.ctx = ctx;
        this.logger = KeelLogger.buildSilentLogger();

        // Keel.logger().debug("do something before call method!");
    }

    protected String readParamForTheFirst(String name, String defaultValue) {
        List<String> strings = ctx.queryParam(name);
        if (strings.size() >= 1) {
            return strings.get(0);
        } else {
            return defaultValue;
        }
    }

    protected String readParamForTheFirst(String name) {
        return readParamForTheFirst(name, null);
    }
}
