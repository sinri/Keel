package io.github.sinri.keel.test.core;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

public class CompKit {

    public static <T extends Comparable<T>> Future<Integer> compareFutures(Future<T> left, Future<T> right) {
        return CompositeFuture.all(
                left,
                right
        ).compose(compositeFuture -> {
            T leftV = compositeFuture.resultAt(0);
            T rightV = compositeFuture.resultAt(1);
            return Future.succeededFuture(leftV.compareTo(rightV));
        });
    }

    public static void main(String[] args) {
    }
}
