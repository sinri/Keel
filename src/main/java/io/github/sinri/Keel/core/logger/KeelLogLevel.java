package io.github.sinri.Keel.core.logger;

/**
 * The Keel Log Level Enum
 */
public enum KeelLogLevel {
    DEBUG, INFO, NOTICE, WARNING, ERROR, FATAL;

    public boolean isMoreSeriousThan(KeelLogLevel standardLevel) {
        return this.ordinal() >= standardLevel.ordinal();
    }

}
