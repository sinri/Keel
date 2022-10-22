package io.github.sinri.keel.servant.endless;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Handler;

/**
 * @since 2.8
 * Experimental
 */
public class KeelEndlessUsingCallback extends KeelVerticle {

    private final long restMS;
    private final Handler<EndCallback> handler;

    public KeelEndlessUsingCallback(long restMS, Handler<EndCallback> handler) {
        this.restMS = restMS;
        this.handler = handler;
    }

    private void routineWrapper() {
        Keel.getVertx().setTimer(
                restMS,
                currentTimerID -> handler.handle(this::routineWrapper)
        );
    }

    @Override
    public void start() throws Exception {
        routineWrapper();
    }

    public interface EndCallback {
        void execute();
    }
}
