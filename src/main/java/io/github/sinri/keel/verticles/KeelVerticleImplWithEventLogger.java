package io.github.sinri.keel.verticles;

import io.github.sinri.keel.logger.event.KeelEventLogger;

import javax.annotation.Nonnull;

/**
 * @since 3.2.0
 */
abstract public class KeelVerticleImplWithEventLogger extends KeelVerticleImplPure {
    private final @Nonnull KeelEventLogger logger;

    public KeelVerticleImplWithEventLogger() {
        super();
        this.logger = buildEventLogger();
    }

    @Nonnull
    public KeelEventLogger getLogger() {
        return logger;
    }

    abstract protected KeelEventLogger buildEventLogger();

}
