package io.github.sinri.keel.servant.sisiodosi;

import io.vertx.core.Handler;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * Experimental!
 * Use Executors.newSingleThreadExecutor().
 *
 * @since 2.8
 */
@Deprecated(since = "2.9", forRemoval = true)
public class KeelSisiodosi3 {
    private final Queue<Handler<Void>> queue;
    private final ExecutorService singleThreadExecutorService;

    public KeelSisiodosi3() {
        this.singleThreadExecutorService = Executors.newSingleThreadExecutor();
        queue = new ConcurrentLinkedQueue<>();
    }

    public void drop(Supplier<Handler<Void>> supplier) {
        this.singleThreadExecutorService.submit(new Callable<Void>() {
            @Override
            public Void call() {

                Handler<Void> rHandler = supplier.get();
                if (rHandler != null) {
                    rHandler.handle(null);
                }
                return null;
            }
        });
    }

    public void stop() {
        this.singleThreadExecutorService.shutdown();
    }
}
