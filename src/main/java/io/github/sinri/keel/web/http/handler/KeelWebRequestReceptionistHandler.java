package io.github.sinri.keel.web.http.handler;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.web.http.receptionist.KeelWebFutureReceptionist;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.InvocationTargetException;

@Deprecated
public class KeelWebRequestReceptionistHandler<R extends KeelWebFutureReceptionist> extends KeelWebRequestHandler {
    private final Class<R> rClass;

    public KeelWebRequestReceptionistHandler(Keel keel, Class<R> rClass) {
        super(keel);
        this.rClass = rClass;
    }

    @Override
    protected KeelEventLogger createLogger() {
        return getKeel().getInstantEventLogger();
    }

    @Override
    protected void handleRequest(RoutingContext routingContext) throws RuntimeException {
        try {
            rClass.getConstructor(Keel.class, RoutingContext.class)
                    .newInstance(getKeel(), routingContext)
                    .handle();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            getLogger().exception(e);
            routingContext.fail(500, e);
        }
    }
}
