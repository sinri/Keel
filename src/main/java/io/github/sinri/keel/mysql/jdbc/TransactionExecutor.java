package io.github.sinri.keel.mysql.jdbc;

public abstract class TransactionExecutor<T> {
    abstract T execute() throws Exception;
}
