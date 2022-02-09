package io.github.sinri.keel.core;

import io.vertx.core.Future;

import java.util.function.Function;

/**
 * This class is used to serve the scenario that one query routine should be packaged for both sync and async use.
 *
 * @param <T>
 * @since 1.9
 */
public class DuplexExecutor<T> {
    protected Function<SyncExecuteResult<T>, SyncExecuteResult<T>> syncExecutor;
    protected Function<Void, Future<T>> asyncExecutor;

    public DuplexExecutor() {
        syncExecutor = null;
        asyncExecutor = null;
    }

    public static <P> DuplexExecutor<P> build(Function<Void, Future<P>> asyncQueryFunction, Function<SyncExecuteResult<P>, SyncExecuteResult<P>> syncQueryFunction) {
        DuplexExecutor<P> dataFetcher = new DuplexExecutor<>();
        dataFetcher.setAsyncExecutor(asyncQueryFunction);
        dataFetcher.setSyncExecutor(syncQueryFunction);
        return dataFetcher;
    }

    public static <P> DuplexExecutor<P> buildAsyncOnly(Function<Void, Future<P>> asyncQueryFunction) {
        DuplexExecutor<P> dataFetcher = new DuplexExecutor<>();
        dataFetcher.setAsyncExecutor(asyncQueryFunction);
        return dataFetcher;
    }

    public static <P> DuplexExecutor<P> buildSyncOnly(Function<SyncExecuteResult<P>, SyncExecuteResult<P>> syncQueryFunction) {
        DuplexExecutor<P> dataFetcher = new DuplexExecutor<>();
        dataFetcher.setSyncExecutor(syncQueryFunction);
        return dataFetcher;
    }

    /**
     * @param asyncExecutor Return future, successful or failed.
     * @return this
     */
    public DuplexExecutor<T> setAsyncExecutor(Function<Void, Future<T>> asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
        return this;
    }

    /**
     * @param syncExecutor Receive an SyncExecuteResult instance as parameter and fulfill its result or error inside, finally return it.
     * @return this
     */
    public DuplexExecutor<T> setSyncExecutor(Function<SyncExecuteResult<T>, SyncExecuteResult<T>> syncExecutor) {
        this.syncExecutor = syncExecutor;
        return this;
    }

    public Future<T> executeAsync() {
        if (asyncExecutor != null) return asyncExecutor.apply(null);
        return Future.failedFuture("DataFetcher::asyncQueryFunction is not set yet");
    }

    /**
     * @return the result if succeed
     * @throws Exception when any ERROR occurs inside
     */
    public T executeSync() throws Exception {
        if (syncExecutor != null) {
            return syncExecutor.apply(new SyncExecuteResult<>()).getResult();
        }
        throw new RuntimeException("DataFetcher::syncQueryFunction is not set yet");
    }

    public static class SyncExecuteResult<R> {
        private Exception error;
        private R result;

        public SyncExecuteResult() {
            error = null;
            result = null;
        }

        public R getResult() throws Exception {
            if (this.error != null) {
                throw this.error;
            }
            return result;
        }

        public void setResult(R result) {
            this.result = result;
        }

        public Exception getError() {
            return error;
        }

        public void setError(Exception error) {
            this.error = error;
        }
    }
}
