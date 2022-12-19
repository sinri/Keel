package io.github.sinri.keel.facade.async;

import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.function.Function;


/**
 * @since 2.9.3
 */
public class FutureRepeat {
    private final KeelTraitForVertxAsync keel;
    private final Function<RoutineResult, Future<Void>> routineFunction;

    private FutureRepeat(KeelTraitForVertxAsync keel, Function<RoutineResult, Future<Void>> routineFunction) {
        this.keel = keel;
        this.routineFunction = routineFunction;
    }

    static Future<Void> call(KeelTraitForVertxAsync keel, Function<RoutineResult, Future<Void>> routineFunction) {
        Promise<Void> promise = Promise.promise();
        RoutineResult routineResult = new RoutineResult(false);
        new FutureRepeat(keel, routineFunction).routine(routineResult, promise);
        return promise.future();
    }

    private void routine(RoutineResult routineResult, Promise<Void> finalPromise) {
        Future.succeededFuture()
                .compose(v -> routineFunction.apply(routineResult))
                .andThen(shouldStopAR -> {
                    if (shouldStopAR.succeeded()) {
                        if (routineResult.isToStop()) {
                            finalPromise.complete();
                        } else {
                            keel.setTimer(1L, x -> routine(routineResult, finalPromise));
                        }
                    } else {
                        finalPromise.fail(shouldStopAR.cause());
                    }
                });
    }

    public static class RoutineResult {
        private boolean toStop;

        public RoutineResult(boolean toStop) {
            this.toStop = toStop;
        }

        public boolean isToStop() {
            return toStop;
        }

        public RoutineResult stop() {
            this.toStop = true;
            return this;
        }
    }
}
