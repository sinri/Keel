package io.github.sinri.keel.verticles;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.AbstractVerticle;

/**
 * 在 Keel 2.0 中，有一个异象，是将所有逻辑扔进 Verticle 里运行来模拟同一线程。
 * <p>
 * 大部分有用的自定义功能都在 KeelVerticleInterface:
 * USE `extends AbstractVerticle implements KeelVerticleInterface`!
 *
 * @since 2.0
 * @since 2.9 deprecated
 */
@Deprecated(since = "2.9")
abstract public class KeelVerticle extends AbstractVerticle implements KeelVerticleInterface {

    private KeelLogger logger = KeelLogger.silentLogger();

    /**
     * @since 2.4 do not rely on context anymore
     * @since 2.8 become public
     */
    public KeelLogger getLogger() {
        return logger;
    }

    /**
     * @since 2.4 do not rely on context anymore
     * @since 2.7 became public
     */
    public final void setLogger(KeelLogger logger) {
        this.logger = logger;
    }
}
