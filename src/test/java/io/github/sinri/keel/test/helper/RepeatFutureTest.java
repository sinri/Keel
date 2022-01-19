package io.github.sinri.keel.test.helper;


import io.github.sinri.keel.core.controlflow.FutureWhile;
import io.vertx.core.Future;

public class RepeatFutureTest {
    public static void main(String[] args) {

        // like sync code
        try {
            int v = 0;
            while (!(v > 5)) {
                v = v + 1;
            }
            System.out.println("done: " + v);
        } catch (Exception e) {
            System.out.println("failed: " + e.getMessage());
        }

        // with future
        new FutureWhile<>(
                0,
                futureWhile -> futureWhile.getLastValue() > 5,
                x -> Future.succeededFuture(x + 1)
        )
                .runInWhile()
                .onFailure(throwable -> System.out.println("failed: " + throwable.getMessage()))
                .onSuccess(fin -> System.out.println("done: " + fin));
    }
}
