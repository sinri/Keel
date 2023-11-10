package io.github.sinri.keel.facade.async;

import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * @since 2.9.4
 * @since 3.0.8 Replace `CompositeFuture.XXX` with `Future.XXX`.
 */
public class FutureForEachParallel {
    private FutureForEachParallel() {

    }

    private static <T> List<Future<Void>> buildFutureList(@Nonnull Iterable<T> collection, @Nonnull Function<T, Future<Void>> itemProcessor) {
        List<Future<Void>> futures = new ArrayList<>();
        collection.forEach(item -> {
            Future<Void> future = Future.succeededFuture().compose(v -> itemProcessor.apply(item));
            futures.add(future);
        });
        return futures;
    }

    public static <T> Future<Void> all(@Nonnull Iterable<T> collection, @Nonnull Function<T, Future<Void>> itemProcessor) {
        List<Future<Void>> futures = buildFutureList(collection, itemProcessor);
        if (futures.isEmpty()) return Future.succeededFuture();
        return Future.all(futures)
                .compose(compositeFuture -> Future.succeededFuture());
    }

    public static <T> Future<Void> any(@Nonnull Iterable<T> collection, @Nonnull Function<T, Future<Void>> itemProcessor) {
        List<Future<Void>> futures = buildFutureList(collection, itemProcessor);
        if (futures.isEmpty()) return Future.succeededFuture();
        return Future.any(futures)
                .compose(compositeFuture -> Future.succeededFuture());
    }

    public static <T> Future<Void> join(@Nonnull Iterable<T> collection, @Nonnull Function<T, Future<Void>> itemProcessor) {
        List<Future<Void>> futures = buildFutureList(collection, itemProcessor);
        if (futures.isEmpty()) return Future.succeededFuture();
        return Future.join(futures)
                .compose(compositeFuture -> Future.succeededFuture());
    }

    public static <T, R> Future<ParallelResult<R>> call(@Nonnull Iterable<T> collection, @Nonnull Function<T, Future<R>> itemProcessor) {
        // transform iterable to a temp map
        AtomicInteger counter = new AtomicInteger(0);
        Map<Integer, T> map = new HashMap<>();
        collection.forEach(item -> {
            map.put(counter.get(), item);
            counter.getAndIncrement();
        });

        // init a ParallelResult
        ParallelResult<R> parallelResult = new ParallelResult<>();

        // each future
        List<Future<R>> futures = new ArrayList<>();
        map.forEach((i, e) -> {
            Future<R> f = Future.succeededFuture()
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

        if (futures.isEmpty()) return Future.succeededFuture(parallelResult);
        // result
        return Future.all(futures)
                .compose(c -> Future.succeededFuture(parallelResult));
    }

    /**
     * @param <R> The result type.
     * @since 3.0.8 Use a certain type R.
     */
    public static class ParallelResult<R> {
        private final Map<Integer, ParallelResultPart<R>> map;

        /**
         * @since 3.0.8 map changed to ConcurrentHashMap from HashMap
         */
        public ParallelResult() {
            this.map = new ConcurrentHashMap<>();
        }

        public int size() {
            return this.map.size();
        }

        public ParallelResult<R> addResultPart(int i, ParallelResultPart<R> parallelResultPart) {
            this.map.put(i, parallelResultPart);
            return this;
        }

        public ParallelResultPart<R> resultAt(int i) {
            return this.map.get(i);
        }

        @Deprecated(since = "3.0.8", forRemoval = true)
        public List<ParallelResultPart<R>> results() {
            return getResultPartList();
        }

        /**
         * @since 3.0.8
         */
        public List<ParallelResultPart<R>> getResultPartList() {
            List<ParallelResultPart<R>> results = new ArrayList<>();
            for (int i = 0; i < this.size(); i++) {
                results.add(resultAt(i));
            }
            return results;
        }

        /**
         * @param fallback Used as result in list when the part failed.
         * @since 3.0.8
         */
        public List<R> getResultList(R fallback) {
            List<R> list = new ArrayList<>();
            getResultPartList().forEach(rp -> list.add(rp.isFailed() ? fallback : rp.getResult()));
            return list;
        }

        /**
         * @since 3.0.8
         */
        public List<R> getResultList() {
            return getResultList(null);
        }

        /**
         * @since 3.0.8
         */
        public boolean isAllSuccessful() {
            for (var v : this.map.values()) {
                if (v.isFailed()) {
                    return false;
                }
            }
            return true;
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
