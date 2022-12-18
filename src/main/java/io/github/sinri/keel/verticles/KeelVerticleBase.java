package io.github.sinri.keel.verticles;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.AbstractVerticle;

import javax.annotation.Nonnull;

abstract public class KeelVerticleBase extends AbstractVerticle implements KeelVerticle {
    private Keel keel;
    private KeelEventLogger logger;

    public KeelVerticleBase() {
        this.logger = KeelEventLogger.silentLogger();
    }

    @Override
    final public Keel getKeel() {
        return keel;
    }

    @Override
    final public void setKeel(Keel keel) {
        this.keel = keel;
    }

    @Override
    final public @Nonnull KeelEventLogger getLogger() {
        return logger;
    }

    final public void setLogger(@Nonnull KeelEventLogger logger) {
        this.logger = logger;
    }
}
