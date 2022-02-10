package io.github.sinri.keel.mysql.exception;

public class KeelSQLResultRowIndexError extends Exception {
    public KeelSQLResultRowIndexError(String message) {
        super(message);
    }

    public KeelSQLResultRowIndexError(String message, Throwable throwable) {
        super(message, throwable);
    }

    public KeelSQLResultRowIndexError(Throwable throwable) {
        super(throwable);
    }
}
