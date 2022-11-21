package io.github.sinri.keel.maids.hourglass;

import io.vertx.core.Handler;

/**
 * It might be used like KeelEndless in standalone mode with Promise and clustered lock.
 *
 * @since 2.9.3
 */
public class KeelPureHourglass extends KeelHourglassImpl {

    private Handler<Long> handler;
    private long interval = 60_000L;

    public KeelPureHourglass() {

        this.handler = event -> {
            getLogger().warning("EMPTY HANDLER");
        };
    }

    public Handler<Long> regularHandler() {
        return handler;
    }

    public KeelHourglass setHandler(Handler<Long> handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public long interval() {
        return interval;
    }


    public KeelPureHourglass setInterval(long interval) {
        this.interval = interval;
        return this;
    }

}
