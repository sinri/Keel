package io.github.sinri.keel.maids.watchman;

import io.github.sinri.keel.verticles.KeelVerticleInterface;
import io.vertx.core.Handler;

/**
 * 集群定时器基础类.
 * For every interval, call once regular handler, in a random node decided by clustered event bus.
 * The three methods defined here should be same throughout nodes in cluster.
 *
 * @since 2.9.3
 */
public interface KeelWatchman extends KeelVerticleInterface {

    String watchmanName();

    long interval();

    Handler<Long> regularHandler();

}
