package io.github.sinri.keel.mysql.jdbc;

public abstract class TransactionExecutor<T> {
    abstract public T execute() throws Exception;
}