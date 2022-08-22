package io.github.sinri.keel.core.controlflow;

import io.vertx.core.Future;

import java.util.function.Supplier;

/**
 * Repeat change future until meet a successful future of TRUE.
 *
 * @since 2.8
 */
public class FutureUntil {
    //private final Function<Void, Future<Boolean>> singleRecursionFunction;
    private final Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier;

//    private FutureUntil(Function<Void, Future<Boolean>> singleRecursionFunction) {
//        this.singleRecursionFunction = singleRecursionFunction;
//    }

    private FutureUntil(Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier) {
        this.singleRecursionForShouldStopSupplier = singleRecursionForShouldStopSupplier;
    }

    /**
     * 1. 建立一个新的世界线 F0；执行【2】
     * 2. 设定世界线 F=F0；执行【3】
     * 3. 基于世界线 F 调用supplier的get方法，并将结果覆盖到世界线 F；执行【4】
     * 4.1. 如果世界线 F 触发了 failure，则返回 Bad End
     * 4.2. 如果世界线 F 触发了 successful True，则返回 世界线 F
     * 4.3. 如果世界线 F 触发了 successful False，则再次执行【2】
     */
    public static Future<Void> call(Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier) {
        return new FutureUntil(singleRecursionForShouldStopSupplier).start();
    }

    private Future<Void> start() {
        Future<Void> future = Future.succeededFuture();
        return recur(future)
                .compose(b -> {
                    return Future.succeededFuture();
                });
    }

    /**
     * @param future 从这个future开始
     * @return 添加了 singleRecursionForShouldStopSupplier 的运行结果后的新future。如果中途failed，就返回 failed future。
     */
    private Future<Boolean> recur(Future<Void> future) {
        return future.compose(v -> {
                    try {
                        return singleRecursionForShouldStopSupplier.get();
                    } catch (Throwable throwable) {
                        return Future.failedFuture(throwable);
                    }
                })
                .compose(outputShouldStop -> {
                    if (!outputShouldStop) {
                        // next
                        return recur(Future.succeededFuture());
                    } else {
                        // stop
                        return Future.succeededFuture();
                    }
                });
    }
}
