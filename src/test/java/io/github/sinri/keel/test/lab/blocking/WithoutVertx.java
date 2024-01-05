package io.github.sinri.keel.test.lab.blocking;

import io.vertx.core.Future;

public class WithoutVertx {
    public static void main(String[] args) {
        var f1 = Future.succeededFuture()
                .compose(v -> {
                    System.out.println("1");
                    return Future.succeededFuture(2);
                })
                .compose(v -> {
                    System.out.println("2: " + v);
                    return Future.failedFuture("3");
                })
                .compose(v -> {
                    System.out.println("3: " + v);
                    return Future.succeededFuture("4 Done");
                }, throwable -> {
                    System.out.println("3: " + throwable);
                    return Future.succeededFuture("4 Failed");
                })
                .compose(v -> {
                    System.out.println("4: " + v);
                    return Future.failedFuture("5");
                });
    }
}
