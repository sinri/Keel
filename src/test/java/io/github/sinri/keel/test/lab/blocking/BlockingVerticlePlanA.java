package io.github.sinri.keel.test.lab.blocking;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.*;

/**
 * 这个解决方案的问题是只能有一个Verticle在worker模式跑，如果有多个异步任务，无法以池模式运行，只能排队。
 */
public class BlockingVerticlePlanA extends KeelVerticleBase {

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

    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions())
                .compose(done -> {
                    KeelEventLogger loggerInEventLoopContext = KeelOutputEventLogCenter.getInstance().createLogger("Sample");

                    BlockingVerticlePlanA futureForBlocking = new BlockingVerticlePlanA();
                    return futureForBlocking.deployMe(new DeploymentOptions()
                                    .setWorker(true)
                            )
                            .compose(deploymentId -> {
                                loggerInEventLoopContext.info(log -> log
                                        .message("deployed: " + deploymentId)
                                        .put("thread_id", Thread.currentThread().getId())
                                );

//                                System.out.println("isWorkerContext: " + futureForBlocking.context.isWorkerContext());
//                                System.out.println("isEventLoopContext: " + futureForBlocking.context.isEventLoopContext());
                                return Future.succeededFuture();
                            })
                            .compose(ready -> {
                                return Future.all(
                                        blockPiece(futureForBlocking, loggerInEventLoopContext),
                                        blockPiece(futureForBlocking, loggerInEventLoopContext)
                                );
                            })
                            .compose(blocked -> {
                                loggerInEventLoopContext.info(log -> log
                                        .message("FIN")
                                        .put("thread_id", Thread.currentThread().getId())
                                );
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

    private static Future<Void> blockPiece(BlockingVerticlePlanA futureForBlocking, KeelEventLogger loggerInEventLoopContext) {
        loggerInEventLoopContext.info(log -> log
                .message("here before executeBlocking handler")
                .put("thread_id", Thread.currentThread().getId())
        );
        return futureForBlocking.executeBlocking(event -> {
            // 在这个scope里，理论上都是在线程池里run
            loggerInEventLoopContext.info(log -> log
                    .message("here in executeBlocking handler")
                    .put("thread_id", Thread.currentThread().getId())
            );
            block(event);
        });
    }

    @Override
    public void start() throws Exception {
        super.start();
        this.setLogger(KeelOutputEventLogCenter.getInstance().createLogger(getClass().getName()));
    }

    public <T> Future<T> executeBlocking(Handler<Promise<T>> promiseHandler) {
        Promise<T> promise = Promise.promise();
        this.context.runOnContext(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                promiseHandler.handle(promise);
            }
        });

        return promise.future();
    }
}
