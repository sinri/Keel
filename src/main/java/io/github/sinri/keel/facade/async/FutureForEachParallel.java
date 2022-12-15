package io.github.sinri.keel.facade.async;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * @since 2.9.4
 */
public class FutureForEachParallel {
    private FutureForEachParallel() {

    }

    public static <T> Future<Void> all(Iterable<T> collection, Function<T, Future<Void>> itemProcessor) {
        List<Future> futures = new ArrayList<>();
        collection.forEach(item -> {
            Future<Void> future = Future.succeededFuture()
                    .compose(v -> itemProcessor.apply(item));
            futures.add(future);
        });
        return CompositeFuture.all(futures)
                .compose(compositeFuture -> Future.succeededFuture());
    }

    public static <T> Future<Void> any(Iterable<T> collection, Function<T, Future<Void>> itemProcessor) {
        List<Future> futures = new ArrayList<>();
        collection.forEach(item -> {
            Future<Void> future = Future.succeededFuture()
                    .compose(v -> itemProcessor.apply(item));
            futures.add(future);
        });
        return CompositeFuture.any(futures)
                .compose(compositeFuture -> Future.succeededFuture());
    }

    public static <T> Future<Void> join(Iterable<T> collection, Function<T, Future<Void>> itemProcessor) {
        List<Future> futures = new ArrayList<>();
        collection.forEach(item -> {
            Future<Void> future = Future.succeededFuture()
                    .compose(v -> itemProcessor.apply(item));
            futures.add(future);
        });
        AtomicReference<FailedInParallel> failed = new AtomicReference<>();
        return CompositeFuture.join(futures)
                .compose(compositeFuture -> Future.succeededFuture());
    }

    public static <T, R> Future<ParallelResult> call(Iterable<T> collection, Function<T, Future<R>> itemProcessor) {
        // transform iterable to a temp map
        AtomicInteger counter = new AtomicInteger(0);
        Map<Integer, T> map = new HashMap<>();
        collection.forEach(item -> {
            map.put(counter.get(), item);
            counter.getAndIncrement();
        });

        // init a ParallelResult
        ParallelResult parallelResult = new ParallelResult();

        // each future
        List<Future> futures = new ArrayList<>();
        map.forEach((i, e) -> {
            Future<Object> f = Future.succeededFuture()
                    .compose(v -> itemProcessor.apply(e))
                    .compose(r -> {
                        ParallelResultPart<R> rParallelResultPart = new ParallelResultPart<>(r);
                        parallelResult.addResultPart(i, rParallelResultPart);
                        return Future.succeededFuture();
                    }, throwable -> {
                        ParallelResultPart<R> rParallelResultPart = new ParallelResultPart<>(throwable);
                        parallelResult.addResultPart(i, rParallelResultPart);
                        return Future.succeededFuture();
                    });
            futures.add(f);
        });

        // result
        return CompositeFuture.all(futures)
                .compose(c -> Future.succeededFuture(parallelResult));
    }

    public static class ParallelResult {
        private final Map<Integer, ParallelResultPart<?>> map;

        public ParallelResult() {
            this.map = new HashMap<>();
        }

        public int size() {
            return this.map.size();
        }

        public <R> ParallelResult addResultPart(int i, ParallelResultPart<R> parallelResultPart) {
            this.map.put(i, parallelResultPart);
            return this;
        }

        public ParallelResultPart<?> resultAt(int i) {
            return this.map.get(i);
        }

        public List<ParallelResultPart<?>> results() {
            List<ParallelResultPart<?>> results = new ArrayList<>();
            for (int i = 0; i < this.size(); i++) {
                results.add(resultAt(i));
            }
            return results;
        }
    }

    public static class ParallelResultPart<R> {
        private final boolean failed;
        private final Throwable cause;
        private final R result;

        public ParallelResultPart(Throwable cause) {
            this.failed = true;
            this.cause = cause;
            this.result = null;
        }

        public ParallelResultPart(R result) {
            this.failed = false;
            this.cause = null;
            this.result = result;
        }

        public R getResult() {
            return result;
        }

        public Throwable getCause() {
            return cause;
        }

        public boolean isFailed() {
            return failed;
        }
    }
}
