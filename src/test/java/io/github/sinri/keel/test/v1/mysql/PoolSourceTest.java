package io.github.sinri.keel.test.v1.mysql;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;

/**
 * 该测试完成了一个实验：
 * 常规的MySQL Pool类在获取Connection时会产生无法获取而超时的情况，
 * 抛出 io.vertx.core.impl.NoStackTraceThrowable : Timeout
 * 自 io.vertx.core.Handler#handle(java.lang.Object)
 * 超时限制长度定义为 this.acquire(current, this.connectionTimeout, promise); 中的
 * connectionTimeout， 默认30s。
 *
 * @since 2.6.1
 */
public class PoolSourceTest {
    private static KeelLogger logger;

    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        logger = Keel.outputLogger("main");

        test_for_inner_exception();
    }

    private static void test_for_connection_timeout() {
        for (int i = 0; i < 512; i++) {
            int finalI = i;
            SharedTestBootstrap.getMySQLKit()
                    .withConnection(sqlConnection -> {
                        logger.info("WORKER [" + finalI + "] STARTED");
                        return sqlConnection.query("select sleep(45) as x").execute()
                                .compose(rows -> {
                                    rows.forEach(row -> {
                                        logger.info("WORKER [" + finalI + "] FETCHED", row.toJson());
                                    });
                                    return Future.succeededFuture();
                                });
                    })
                    .onComplete(asyncResult -> {
                        if (asyncResult.failed()) {
                            logger.exception("WORKER [" + finalI + "] FAILED", asyncResult.cause());
                        } else {
                            logger.info("WORKER [" + finalI + "] DONE");
                        }
                    });
        }
    }

    private static void test_for_idle_timeout() {
        // 这个实验确认了idle的特性：
        //  并不会因为线程sleep而close掉sql connection
        logger.info("test_for_idle_timeout start");
        SharedTestBootstrap.getMySQLKit()
                .withConnection(sqlConnection -> {
                    logger.info("got sql connection");
                    return sqlConnection.query("select sleep(18) as x")
                            .execute()
                            .compose(rows -> {
                                logger.info("ran sql for 18s over: " + rows.toString());
                                return Future.succeededFuture();
                            })
                            .compose(v -> {
                                try {
                                    Thread.sleep(19 * 1000);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                                logger.info("slept for 19s");
                                return sqlConnection.query("select 1 as y")
                                        .execute()
                                        .compose(rows -> {
                                            logger.info("ran sql after sleeping: " + rows.toString());
                                            return Future.succeededFuture();
                                        });
                            });
                })
                .onFailure(throwable -> {
                    logger.exception(throwable);
                });
    }

    private static void test_for_inner_exception() {
        SharedTestBootstrap.getMySQLKit().withTransaction(sqlConnection -> {
                    throw new RuntimeException("INNER ERROR 1");
                })
                .onFailure(throwable -> {
                    logger.exception("OUTSIDE TAIL", throwable);
                });
    }
}
