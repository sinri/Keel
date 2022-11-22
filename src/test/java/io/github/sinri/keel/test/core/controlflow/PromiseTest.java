package io.github.sinri.keel.test.core.controlflow;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Promise;

public class PromiseTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v -> {
            Promise<Object> promise = Promise.promise();
            Keel.getVertx().setTimer(1000L, x -> {
                Keel.outputLogger().info("TIMEOUT");
                promise.fail("timeout");
            });
            promise.future()
                    .andThen(ar -> {
                        if (ar.failed()) {
                            Keel.outputLogger().exception(ar.cause());
                        } else {
                            Keel.outputLogger().info("DONE");
                        }
                        Keel.getVertx().close();
                    });
        });


    }
}
