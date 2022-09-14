package io.github.sinri.keel.test.servant;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.servant.endless.KeelEndless;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;

import java.util.UUID;


public class EndlessTest {
    private static Future<Void> blockMode() {
        String s = UUID.randomUUID().toString();
        Keel.outputLogger("endless").info("task [" + s + "] start");
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Keel.outputLogger("endless").info("task [" + s + "] end");
        return Future.succeededFuture();
    }

    private static Future<Void> nonBlockMode() {
        String s = UUID.randomUUID().toString();
        Keel.outputLogger("endless").info("task [" + s + "] start");
        Keel.getVertx().setTimer(2000L, timerID -> {
            Keel.outputLogger("endless").info("task [" + s + "] end");
        });
        return Future.succeededFuture();
    }

    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        new KeelEndless(
                1000L,
                EndlessTest::blockMode
        ).deployMe();
    }
}
