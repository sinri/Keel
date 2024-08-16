package io.github.sinri.keel.mysql.exception;

/**
 * @since 4.0.0 rename to KeelMySQLResultRowIndexError
 */
public class KeelMySQLResultRowIndexError extends Exception {
    public KeelMySQLResultRowIndexError(String message) {
        super(message);
    }

    public KeelMySQLResultRowIndexError(String message, Throwable throwable) {
        super(message, throwable);
    }

    public KeelMySQLResultRowIndexError(Throwable throwable) {
        super(throwable);
    }
}
