package io.github.sinri.keel.web.service;

import io.vertx.ext.web.RoutingContext;

/**
 * @since 2.8.1
 */
abstract public class KeelWebRequestHandlerImplBase implements KeelWebRequestHandler {
    private RoutingContext routingContext;
    private boolean verbose = false;

    public RoutingContext getRoutingContext() {
        return routingContext;
    }

    public void setRoutingContext(RoutingContext routingContext) {
        this.routingContext = routingContext;
    }

    public boolean getVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public final void handle(RoutingContext routingContext) {
        setRoutingContext(routingContext);
        handleRequest();
    }
}
