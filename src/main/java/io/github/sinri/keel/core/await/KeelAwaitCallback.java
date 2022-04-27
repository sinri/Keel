package io.github.sinri.keel.core.await;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.web.KeelHttpServer;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.client.WebClient;

import java.util.Date;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @since 2.1
 */
public class KeelAwaitCallback {
    //private static final ExecutorService executor = Executors.newCachedThreadPool();
//    private static final KeelAwaitCallback instance = new KeelAwaitCallback();

    public KeelAwaitCallback() {
    }

    public static <T> T awaitCallableTask(Callable<T> task) throws ExecutionException, InterruptedException {
        return new KeelAwaitCallback().await(task);
    }

//    protected static KeelAwaitCallback getInstance() {
//        return instance;
//    }

    public static <T> T awaitFunction(Function<Void, T> func) throws ExecutionException, InterruptedException {
        return new KeelAwaitCallback().await(new Callable<T>() {
            @Override
            public T call() throws Exception {
                return func.apply(null);
            }
        });
    }

    public static <T> T awaitVertxFuture(Future<T> future) throws ExecutionException, InterruptedException {
        CallableWrapperForVertxFuture<T> task = new CallableWrapperForVertxFuture<T>();

        future
                .compose(t -> {
                    task.write(t, null);
                    return Future.succeededFuture();
                })
                .recover(throwable -> {
                    task.write(null, throwable);
                    return Future.succeededFuture();
                });

        return new KeelAwaitCallback().await(task);
    }

    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions());

        KeelLogger logger = Keel.outputLogger("KeelAwaitCallback");

        KeelHttpServer keelHttpServer = new KeelHttpServer(Keel.getVertx(), new HttpServerOptions().setPort(9999), true);
        keelHttpServer.getRouter().route("/").blockingHandler(ctx -> {
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ctx.response().end(String.valueOf(new Date().getTime()));
        });
        keelHttpServer.listen();

        logger.info("start: " + new Date().getTime());
        Future<Long> future = Future.succeededFuture()
                .compose(v -> {
                    return WebClient.create(Keel.getVertx())
                            .get(9999, "localhost", "/")
                            .send()
                            .compose(bufferHttpResponse -> {
                                logger.info("response come: " + bufferHttpResponse.bodyAsString());
                                return Future.succeededFuture();
                            });
                })
                .compose(v -> {
                    return WebClient.create(Keel.getVertx())
                            .get(9999, "localhost", "/")
                            .send()
                            .compose(bufferHttpResponse -> {
                                logger.info("response come: " + bufferHttpResponse.bodyAsString());
                                return Future.succeededFuture();
                            });
                });
//                .compose(v -> {
//                    try {
//                        logger.info("inside future a1");
//                        Thread.sleep(2000L);
//                        logger.info("inside future a2");
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    return Future.succeededFuture(new Date().getTime());
//                })
//                .compose(v -> {
//                    try {
//                        logger.info("inside future b1");
//                        Thread.sleep(2000L);
//                        logger.info("inside future b2");
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    return Future.succeededFuture(new Date().getTime());
//                });
        try {
            Long end = KeelAwaitCallback.awaitVertxFuture(future);
            logger.info(" end : " + end);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("close vertx");
        Keel.closeVertx();
    }

    public <T> T await(Callable<T> task) throws ExecutionException, InterruptedException {
        // 可能阻塞
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            return executor.submit(task).get();
        } finally {
            executor.shutdown();
        }
    }

//    public static void closePool() {
//        executor.shutdown();
//    }

    private static class CallableWrapperForVertxFuture<T> implements Callable<T> {
        private final AtomicBoolean finishedRef = new AtomicBoolean(false);
        private final AtomicReference<T> tRef = new AtomicReference<>(null);
        private final AtomicReference<Throwable> throwableRef = new AtomicReference<>(null);

        @Override
        public T call() throws Exception {
            return read();
        }

        public synchronized T read() throws ExecutionException, InterruptedException {
            System.out.println("CallableWrapperForVertxFuture::read start");
            while (!finishedRef.get()) {
                System.out.println("CallableWrapperForVertxFuture::read inside while [1]");
                if (throwableRef.get() != null) {
                    throw new ExecutionException(throwableRef.get());
                }
                System.out.println("CallableWrapperForVertxFuture::read inside while [2] to wait");
                this.wait();
                System.out.println("CallableWrapperForVertxFuture::read inside while [3] wait called");
            }
            return tRef.get();
        }

        public synchronized void write(T t, Throwable e) {
            System.out.println("CallableWrapperForVertxFuture::write [1]");
            tRef.set(t);
            throwableRef.set(e);
            finishedRef.set(true);
            System.out.println("CallableWrapperForVertxFuture::write [2] to notify");
            this.notify();
            System.out.println("CallableWrapperForVertxFuture::write [3] notify called");
        }

//        public boolean isFinished() {
//            return finishedRef.get();
//        }
//
//        public T getResult() {
//            return tRef.get();
//        }
//
//        public Throwable getThrowable() {
//            return throwableRef.get();
//        }
    }
}
