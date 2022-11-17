package io.github.sinri.keel.test.web2;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.web.service.KeelWebRequestFutureHandler;
import io.vertx.ext.web.RoutingContext;

abstract public class Service extends KeelWebRequestFutureHandler {


    public Service() {
        setVerbose(true);
        Keel.outputLogger().info("SERVICE INIT");
    }

    @Override
    public KeelLogger createLogger(RoutingContext routingContext) {
        return Keel.outputLogger();
    }
}
