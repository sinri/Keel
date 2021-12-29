package io.github.sinri.keel.core.logger;

/**
 * The Keel Log Level Enum
 */
public enum KeelLogLevel {
    DEBUG, INFO, NOTICE, WARNING, ERROR, FATAL;

    public boolean isMoreSeriousThan(KeelLogLevel standardLevel) {
        return this.ordinal() >= standardLevel.ordinal();
    }

}
