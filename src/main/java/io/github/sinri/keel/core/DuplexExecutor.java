package io.github.sinri.keel.core;

import io.vertx.core.Future;

/**
 * @param <T>
 * @param <A>
 * @param <S>
 * @since 1.13
 */
@Deprecated(since = "3.0.17", forRemoval = true)
public class DuplexExecutor<T, A, S, E extends Throwable> {
    protected AsyncExecutor<T, A> asyncExecutor;
    protected SyncExecutor<T, S, E> syncExecutor;

    public DuplexExecutor(AsyncExecutor<T, A> asyncExecutor, SyncExecutor<T, S, E> syncExecutor) {
        this.asyncExecutor = asyncExecutor;
        this.syncExecutor = syncExecutor;
    }

    protected void setAsyncExecutor(AsyncExecutor<T, A> asyncExecutor) {
        this.asyncExecutor = asyncExecutor;
    }

    protected void setSyncExecutor(SyncExecutor<T, S, E> syncExecutor) {
        this.syncExecutor = syncExecutor;
    }

    public Future<T> executeAsync(A a) {
        return this.asyncExecutor.execute(a);
    }

    public T executeSync(S s) throws E {
        return this.syncExecutor.execute(s);
    }

    public interface AsyncExecutor<T, A> {
        Future<T> execute(A a);
    }

    public interface SyncExecutor<T, S, E extends Throwable> {
        T execute(S s) throws E;
    }

}
