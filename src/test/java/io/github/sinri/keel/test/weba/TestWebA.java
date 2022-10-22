package io.github.sinri.keel.test.weba;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureForRange;
import io.github.sinri.keel.servant.endless.KeelEndlessUsingCallback;
import io.github.sinri.keel.servant.sisiodosi.KeelSisiodosi2;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.web.KeelHttpServer;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.function.Supplier;

public class TestWebA {
    static int requestQueueLimit = 5;

    public static void main2(String[] args) {
        SharedTestBootstrap.initialize();

        Queue<RoutingContext> queue = new ConcurrentLinkedQueue<>();

        var endlessUsingCallback = new KeelEndlessUsingCallback(100L, new Handler<KeelEndlessUsingCallback.EndCallback>() {
            @Override
            public void handle(KeelEndlessUsingCallback.EndCallback endCallback) {
                Keel.executeWithinLock(
                                "a1",
                                () -> Future.succeededFuture(queue.poll())
                        )
                        .compose(routingContext -> {
                            if (routingContext == null) {
                                return Future.succeededFuture();
                            }
                            return Future.succeededFuture()
                                    .compose(v -> {
                                        return Keel.getMySQLKit().withConnection(sqlConnection -> {
                                            return sqlConnection.query("select sleep(1)")
                                                    .execute()
                                                    .compose(rows -> {
                                                        return Future.succeededFuture();
                                                    });
                                        });
                                    })
                                    .compose(v -> {
                                        routingContext.json(new JsonObject().put("code", "OK"));
                                        return Future.succeededFuture();
                                    });
                        })
                        .onComplete(ar -> {
                            endCallback.execute();
                        });
            }
        });
        Keel.getVertx().deployVerticle(endlessUsingCallback, new DeploymentOptions().setWorker(true));

        new KeelHttpServer(
                new HttpServerOptions().setPort(9800),
                true
        )
                .configureRoutes(router -> {
                    router.errorHandler(503, routingContext -> {
                        routingContext.response()
//                                .setStatusCode(503)
                                .setStatusMessage(routingContext.failure().getMessage())
                                .end();
                    });
                    router.get("/").handler(routingContext -> {
                        Keel.executeWithinLock(
                                "a1",
                                () -> {
                                    if (queue.size() >= requestQueueLimit) {
                                        routingContext.fail(503, new Exception("over"));
                                    } else {
                                        queue.offer(routingContext);
                                    }
                                    return Future.succeededFuture();
                                }
                        );
                    });
                })
                .listen();

        runRequests(new Supplier<Integer>() {
            @Override
            public Integer get() {
                return queue.size();
            }
        });
    }

    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        KeelSisiodosi2 sisiodosi = new KeelSisiodosi2("weba");

        new KeelHttpServer(
                new HttpServerOptions().setPort(9800),
                true
        )
                .configureRoutes(router -> {
                    router.errorHandler(500, routingContext -> {
                        routingContext.response()
//                                .setStatusCode(503)
                                .setStatusMessage(routingContext.failure().getMessage())
                                .end();
                    });
                    router.get("/").handler(routingContext -> {
                        var size = sisiodosi.estimateDripQueueSize();
                        if (size >= requestQueueLimit) {
                            routingContext.fail(new Exception("over"));
                        } else {
                            sisiodosi.drop(new Supplier<Future<Void>>() {
                                @Override
                                public Future<Void> get() {
                                    routingContext.json(new JsonObject().put("code", "OK"));
                                    return Future.succeededFuture();
                                }
                            });
                        }
                    });
                })
                .listen();

        runRequests(new Supplier<Integer>() {
            @Override
            public Integer get() {
                return -1;
            }
        });
    }

    private static void runRequests(Supplier<Integer> queueSizeSupplier) {
        FutureForRange.call(10, new Function<Integer, Future<Void>>() {
            @Override
            public Future<Void> apply(Integer j) {
                int x = 1 + (int) (9 * Math.random());

                for (int i = 0; i < x; i++) {
                    int finalI = i;
                    //Keel.outputLogger("client").info("[" + finalI + "] start, queue size: " + queueSizeSupplier.get());
                    WebClient.create(Keel.getVertx())
                            .get(9800, "127.0.0.1", "/")
                            .send()
                            .onSuccess(bufferHttpResponse -> {
                                Keel.outputLogger("client").info("[" + j + ":" + finalI + "] " + bufferHttpResponse.statusCode() + " : " + bufferHttpResponse.statusMessage());
                            });
                }

                try {
                    Thread.sleep(900L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                return Future.succeededFuture();
            }
        });

    }
}
