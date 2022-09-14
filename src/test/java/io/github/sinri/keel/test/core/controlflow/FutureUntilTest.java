package io.github.sinri.keel.test.core.controlflow;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureUntil;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicInteger;

public class FutureUntilTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        KeelLogger logger = Keel.outputLogger("FutureUntilTest");

        AtomicInteger atomicInteger = new AtomicInteger(0);

        FutureUntil.call(() -> {
                    int i = atomicInteger.incrementAndGet();
                    logger.info("FutureUntil calling, i=" + atomicInteger.get());
                    if (i == 5) {
                        return Future.failedFuture("GIVE ME FIVE");
                        // throw new RuntimeException("GIVE ME FIVE");
                        // return Future.succeededFuture(true);
                    }
                    return Future.succeededFuture(false);
                })
                .onSuccess(v -> {
                    logger.info("FutureUntil called, i=" + atomicInteger.get());
                })
                .onFailure(throwable -> {
                    logger.exception("STOPPED WITH ERROR", throwable);

                })
                .eventually(v -> {
                    return Keel.getVertx().close();
                });
    }
}
