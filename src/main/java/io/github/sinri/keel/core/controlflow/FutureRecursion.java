package io.github.sinri.keel.core.controlflow;

import io.vertx.core.Future;

import java.util.function.Function;

/**
 * 使用递归传递Future的方式实现了不可预测的中止条件下的异步循环体。
 *
 * @param <T> 循环依据
 * @since 1.13
 */
public class FutureRecursion<T> {
    private final Function<T, Future<Boolean>> shouldNextFunction;
    private final Function<T, Future<T>> singleRecursionFunction;

    private FutureRecursion(Function<T, Future<Boolean>> shouldNextFunction, Function<T, Future<T>> singleRecursionFunction) {
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
                        })
                        .onFailure(throwable -> {
                            throw new RuntimeException("FutureRecursion::recur failed in routine", throwable);
                        })
                );
    }

    public static <T> Future<T> call(
            T initValue,
            Function<T, Future<Boolean>> shouldNextFunction,
            Function<T, Future<T>> singleRecursionFunction
    ) {
        return new FutureRecursion<T>(
                shouldNextFunction, singleRecursionFunction
        )
                .run(initValue);
    }

    private Future<T> run(T initValue) {
        return recur(Future.succeededFuture(initValue));
    }
}
