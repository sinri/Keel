package io.github.sinri.keel.test.v1.helper;

import io.vertx.core.Future;

public class ComposerRuntimeExceptionTest {
    public static void main(String[] args) {
        Future.succeededFuture()
                .compose(v1 -> {
                    System.out.println("v1 !");
                    return Future.succeededFuture("v2");
                })
                .compose(v2 -> {
                    System.out.println("v2 !");
                    if (v2.equals("v2"))
                        throw new RuntimeException(v2 + " error");
                    return Future.succeededFuture("v3");
                })
                .compose(v3 -> {
                    System.out.println("v3 !");
                    return Future.succeededFuture();
                })
                .onFailure(throwable -> {
                    System.out.println("Throwable: " + throwable);
                })
                .onSuccess(x -> {
                    System.out.println("x: " + x);
                });
    }
}
