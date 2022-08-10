package io.github.sinri.keel.test.core.logger;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureForRange;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.core.logger.KeelLoggerOptions;
import io.github.sinri.keel.core.logger.impl.KeelEnqueueAsyncLogger;
import io.github.sinri.keel.servant.endless.KeelEndless;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class KeelEnqueueAsyncLoggerTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        Queue<String> queue = new ConcurrentLinkedQueue<>();
        KeelEnqueueAsyncLogger logger = new KeelEnqueueAsyncLogger(
                new KeelLoggerOptions()
                        .loadForAspect("async"),
                queue
        );
        KeelLogger outputLogger = Keel.outputLogger("endless-logger");

        KeelEndless endless = new KeelEndless(3000L, () -> {
            while (true) {
                String item = queue.poll();
                outputLogger.notice("POLL", new JsonObject().put("item", item));
                if (item == null) break;
                outputLogger.text(item);
            }
            return Future.succeededFuture();
        });
        endless.deployMe(new DeploymentOptions().setWorker(true))
                .compose(deploymentID -> {
                    outputLogger.info("deploymentID " + deploymentID);
                    return FutureForRange.call(20, i -> {
                        long timer = Keel.getVertx().setTimer((i + 1) * 1000L, timerID -> {
                            logger.info("i=" + i);
                        });
                        outputLogger.info("set timer " + i + " -> " + timer);
                        return Future.succeededFuture();
                    });
                });

        Keel.getVertx().setTimer(15 * 1000L, timerID -> {
            outputLogger.notice("FIN");
            Keel.getVertx().close();
        });


    }
}
