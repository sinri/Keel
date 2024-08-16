package io.github.sinri.keel.mysql.exception;

/**
 * @since 1.1
 * @since 4.0.0 rename to KeelMySQLGenerateError
 */
public class KeelMySQLGenerateError extends RuntimeException {
    public KeelMySQLGenerateError(String s) {
        super(s);
    }
}
