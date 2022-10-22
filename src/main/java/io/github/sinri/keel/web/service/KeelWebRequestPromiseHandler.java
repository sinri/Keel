package io.github.sinri.keel.web.service;

import io.github.sinri.keel.Keel;
import io.vertx.core.Promise;

public abstract class KeelWebRequestPromiseHandler extends KeelWebRequestHandler {
    private Long timerID;

    protected long timeout() {
        return 30_000L;
    }

    /**
     * @param promise build response for this request
     */
    abstract protected void handleRequestForFuture(Promise<Object> promise);

    @Override
    public final void handleRequest() {
        Promise<Object> respPromise = Promise.promise();
        timerID = Keel.getVertx().setTimer(timeout(), v -> {
            timerID = null;
            respPromise.tryFail(new Timeout(timeout()));
        });
        handleRequestForFuture(respPromise);
        respPromise.future()
                .onSuccess(this::respondOnSuccess)
                .onFailure(this::respondOnFailure);
    }

    @Override
    protected void respondOnFailure(Throwable throwable) {
        if (throwable instanceof Timeout) {
            getRoutingContext().fail(504, throwable);
        } else {
            super.respondOnFailure(throwable);
        }
    }

    public static class Timeout extends Exception {
        public Timeout(long t) {
            super("WAITED " + t + " ms");
        }
    }
}
