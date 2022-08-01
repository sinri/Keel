package io.github.sinri.keel.mysql.exception;

/**
 * @since 2.8
 */
public class KeelMySQLException extends Exception {
    public KeelMySQLException(String msg) {
        super(msg);
    }

    public KeelMySQLException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public KeelMySQLException(Throwable cause) {
        super(cause);
    }
}
