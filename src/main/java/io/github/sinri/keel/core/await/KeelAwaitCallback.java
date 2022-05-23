package io.github.sinri.keel.core.await;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;

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

    public KeelAwaitCallback() {
    }

    public static <T> T awaitCallableTask(Callable<T> task) throws ExecutionException, InterruptedException {
        return new KeelAwaitCallback().await(task);
    }

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

    public <T> T await(Callable<T> task) throws ExecutionException, InterruptedException {
        // 可能阻塞
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            return executor.submit(task).get();
        } finally {
            executor.shutdown();
        }
    }

    private static class CallableWrapperForVertxFuture<T> implements Callable<T> {
        private final AtomicBoolean finishedRef = new AtomicBoolean(false);
        private final AtomicReference<T> tRef = new AtomicReference<>(null);
        private final AtomicReference<Throwable> throwableRef = new AtomicReference<>(null);

        private final KeelLogger logger;

        public CallableWrapperForVertxFuture() {
            this.logger = Keel.outputLogger(this.getClass().getName());
        }

        public CallableWrapperForVertxFuture(KeelLogger logger) {
            this.logger = logger;
        }

        @Override
        public T call() throws Exception {
            return read();
        }

        public synchronized T read() throws ExecutionException, InterruptedException {
            logger.debug("CallableWrapperForVertxFuture::read start");
            while (!finishedRef.get()) {
                logger.debug("CallableWrapperForVertxFuture::read inside while [1]");
                if (throwableRef.get() != null) {
                    throw new ExecutionException(throwableRef.get());
                }
                logger.debug("CallableWrapperForVertxFuture::read inside while [2] to wait");
                this.wait();
                logger.debug("CallableWrapperForVertxFuture::read inside while [3] wait called");
            }
            return tRef.get();
        }

        public synchronized void write(T t, Throwable e) {
            logger.debug("CallableWrapperForVertxFuture::write [1]");
            tRef.set(t);
            throwableRef.set(e);
            finishedRef.set(true);
            logger.debug("CallableWrapperForVertxFuture::write [2] to notify");
            this.notify();
            logger.debug("CallableWrapperForVertxFuture::write [3] notify called");
        }
    }
}
