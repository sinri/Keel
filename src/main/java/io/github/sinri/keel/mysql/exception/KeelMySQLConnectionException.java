package io.github.sinri.keel.mysql.exception;

public class KeelMySQLConnectionException extends KeelMySQLException {
    public KeelMySQLConnectionException(String msg) {
        super(msg);
    }

    public KeelMySQLConnectionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public KeelMySQLConnectionException(Throwable cause) {
        super(cause);
    }
}
