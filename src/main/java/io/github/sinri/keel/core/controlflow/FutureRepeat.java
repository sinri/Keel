package io.github.sinri.keel.core.controlflow;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.util.function.Function;


/**
 * @since 2.9.3
 */
public class FutureRepeat {
    private final Function<RoutineResult, Future<Void>> routineFunction;

    private FutureRepeat(Function<RoutineResult, Future<Void>> routineFunction) {
        this.routineFunction = routineFunction;
    }

    public static Future<Void> call(Function<RoutineResult, Future<Void>> routineFunction) {
        Promise<Void> promise = Promise.promise();
        RoutineResult routineResult = new RoutineResult(false);
        new FutureRepeat(routineFunction).routine(routineResult, promise);
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
                            Keel.getVertx().setTimer(1L, x -> routine(routineResult, finalPromise));
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

//    public static void main(String[] args) {
//        Keel.initializeStandaloneVertx(new VertxOptions())
//                .onSuccess(v0 -> {
//                    AtomicInteger atomicInteger = new AtomicInteger(0);
//
//                    FutureRepeat.call(routineResult -> {
//                                int i = atomicInteger.incrementAndGet();
//                                if (i > 5) {
//                                    routineResult.stop();
//                                }
//                                return Future.succeededFuture();
//                            })
//                            .onSuccess(v -> {
//                                System.out.println("Final I=" + atomicInteger.get());
//                                Keel.getVertx().close();
//                            });
//                });
//    }
}
