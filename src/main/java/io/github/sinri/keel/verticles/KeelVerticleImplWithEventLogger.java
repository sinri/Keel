package io.github.sinri.keel.verticles;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.jetbrains.annotations.NotNull;


/**
 * @since 3.2.0
 */
abstract public class KeelVerticleImplWithEventLogger extends AbstractVerticle implements KeelVerticle {
    private @NotNull KeelEventLogger logger;

    public KeelVerticleImplWithEventLogger() {
        super();
        this.logger = buildEventLogger();
    }

    @NotNull
    public KeelEventLogger getLogger() {
        return logger;
    }

    abstract protected KeelEventLogger buildEventLogger();

    @Override
    public final void start(Promise<Void> startPromise) {
        this.logger = buildEventLogger();
        startAsKeelVerticle(startPromise);
    }

    @Override
    public final void start() {
        this.logger = buildEventLogger();
        this.startAsKeelVerticle();
    }

    protected void startAsKeelVerticle(Promise<Void> startPromise) {
        startAsKeelVerticle();
        startPromise.complete();
    }

    abstract protected void startAsKeelVerticle();
}
