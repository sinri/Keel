package io.github.sinri.keel.logger;


import org.jetbrains.annotations.NotNull;

public enum KeelLogLevel {
    DEBUG, INFO, NOTICE, WARNING, ERROR, FATAL, SILENT;

    public boolean isEnoughSeriousAs(@NotNull KeelLogLevel standardLevel) {
        return this.ordinal() >= standardLevel.ordinal();
    }

    public boolean isNegligibleThan(@NotNull KeelLogLevel standardLevel) {
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
