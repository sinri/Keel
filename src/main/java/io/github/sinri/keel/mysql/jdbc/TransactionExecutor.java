package io.github.sinri.keel.mysql.jdbc;

@Deprecated
public abstract class TransactionExecutor<T> {
    abstract public T execute() throws Exception;
}
