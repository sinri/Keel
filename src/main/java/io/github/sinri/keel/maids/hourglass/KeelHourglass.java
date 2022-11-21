package io.github.sinri.keel.maids.hourglass;

import io.github.sinri.keel.verticles.KeelVerticleInterface;
import io.vertx.core.Handler;

/**
 * 集群定时器基础类.
 * For every interval, call once regular handler, in a random node decided by clustered event bus.
 *
 * @since 2.9.3
 */
public interface KeelHourglass extends KeelVerticleInterface {

    default String hourglassName() {
        return deploymentID();
    }

    long interval();

    Handler<Long> regularHandler();

}
