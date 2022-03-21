package io.github.sinri.keel.core.controlflow;

import io.vertx.core.Future;

import java.util.function.Function;

/**
 * 使用递归传递Future的方式实现了不可预测的中止条件下的异步循环体
 *
 * @param <T> 循环依据
 * @since 1.13
 */
public class FutureRecursion<T> {
    private final Function<T, Future<Boolean>> shouldNextFunction;
    private final Function<T, Future<T>> singleRecursionFunction;

    public FutureRecursion(Function<T, Future<Boolean>> shouldNextFunction, Function<T, Future<T>> singleRecursionFunction) {
        this.shouldNextFunction = shouldNextFunction;
        this.singleRecursionFunction = singleRecursionFunction;
    }

    private Future<T> recur(Future<T> previousFuture) {
        return previousFuture
                .compose(previousT -> this.shouldNextFunction.apply(previousT)
                        .compose(shouldNext -> {
                            if (shouldNext) {
                                return recur(singleRecursionFunction.apply(previousT));
                            } else {
                                return Future.succeededFuture(previousT);
                            }
                        }));
    }

    public Future<T> run(T initValue) {
        return recur(Future.succeededFuture(initValue));
    }
}
