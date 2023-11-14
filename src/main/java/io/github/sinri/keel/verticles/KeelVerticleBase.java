package io.github.sinri.keel.verticles;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.AbstractVerticle;

import javax.annotation.Nonnull;

abstract public class KeelVerticleBase extends AbstractVerticle implements KeelVerticle {
    private KeelEventLogger logger;

    public KeelVerticleBase() {
        this.logger = KeelEventLogger.silentLogger();
    }

    @Override
    final public @Nonnull KeelEventLogger getLogger() {
        return logger;
    }

    final public void setLogger(@Nonnull KeelEventLogger logger) {
        this.logger = logger;
    }

    @Override
    abstract public void start() throws Exception;
}
