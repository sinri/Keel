package io.github.sinri.keel.core.logger;

/**
 * The Keel Log Level Enum
 * Since 1.10 the SILENT added
 */
@Deprecated(since = "2.6", forRemoval = true)
public enum KeelLogLevel {
    DEBUG, INFO, NOTICE, WARNING, ERROR, FATAL, SILENT;

    public boolean isMoreSeriousThan(KeelLogLevel standardLevel) {
        return this.ordinal() >= standardLevel.ordinal();
    }

    public boolean isNegligibleThan(KeelLogLevel standardLevel) {
        return this.ordinal() < standardLevel.ordinal();
    }

    /**
     * @return should always be silent
     * @since 1.10
     */
    public boolean isSilent() {
        return this.ordinal() >= SILENT.ordinal();
    }
}
