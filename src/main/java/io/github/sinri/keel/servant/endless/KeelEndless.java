package io.github.sinri.keel.servant.endless;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;

import java.util.function.Supplier;

/**
 * @since 2.7
 */
public class KeelEndless extends KeelVerticle {
    private final long restMS;
    private final Supplier<Future<Void>> supplier;

    public KeelEndless(long restMS, Supplier<Future<Void>> supplier) {
        this.restMS = restMS;
        this.supplier = supplier;
    }

    public Future<Void> routine() {
        return supplier.get();
    }

    public void routineWrapper() {
        Keel.getVertx().setTimer(restMS, timerID -> {
            routine().onComplete(done -> routineWrapper());
        });
    }

    @Override
    public void start() throws Exception {
        routineWrapper();
    }
}
