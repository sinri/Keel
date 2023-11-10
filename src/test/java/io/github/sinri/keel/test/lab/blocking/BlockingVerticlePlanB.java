package io.github.sinri.keel.test.lab.blocking;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.*;

/**
 * 可以多线程运行。
 */
public class BlockingVerticlePlanB {
    private static Future<Void> executeBlocking(Handler<Promise<Void>> blockCode) {
        KeelEventLogger logger = KeelOutputEventLogCenter.getInstance().createLogger("Sample");
        Promise<Void> promise = Promise.promise();
        KeelVerticleBase verticle = new KeelVerticleBase() {
            @Override
            public void start() throws Exception {
                super.start();

                this.setLogger(logger);

                getLogger().info("in verticle " + deploymentID());
                blockCode.handle(promise);

                promise.future()
                        .onComplete(ar -> {
                            this.undeployMe();
                        });
            }
        };
        return verticle.deployMe(new DeploymentOptions().setWorker(true))
                .compose(deploymentId -> {
                    logger.info("deployed: " + deploymentId);
                    return promise.future();
                });
    }

    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions())
                .compose(done -> {
                    KeelEventLogger loggerInEventLoopContext = KeelOutputEventLogCenter.getInstance().createLogger("Sample");

                    loggerInEventLoopContext.info(log -> log
                            .message("init")
                            .put("thread_id", Thread.currentThread().getId())
                    );

                    return Future.all(
                                    blockPiece(loggerInEventLoopContext),
                                    blockPiece(loggerInEventLoopContext)
                            )
                            .compose(compositeFuture -> {
                                loggerInEventLoopContext.info("FIN");
                                return Future.succeededFuture();
                            });
                })

                .onFailure(throwable -> {
                    throwable.printStackTrace();
                })
                .eventually(v -> {
                    return Keel.close();
                });
    }

    private static Future<Void> blockPiece(KeelEventLogger loggerInEventLoopContext) {
        return executeBlocking(new Handler<Promise<Void>>() {
            @Override
            public void handle(Promise<Void> event) {
                loggerInEventLoopContext.info(log -> log
                        .message("blockPiece::handle before block")
                        .put("thread_id", Thread.currentThread().getId())
                );
                block(event);
            }
        });
    }

    private static void block(Promise<Void> promise) {
        KeelEventLogger loggerInBlockingContext = KeelOutputEventLogCenter.getInstance().createLogger("Sample");
        loggerInBlockingContext.info(log -> log.message("START").put("thread_id", Thread.currentThread().getId()));
        try {
            Thread.sleep(30_000L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        loggerInBlockingContext.info(log -> log.message("END").put("thread_id", Thread.currentThread().getId()));
        promise.complete();
    }
}
