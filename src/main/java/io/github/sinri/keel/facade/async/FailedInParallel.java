package io.github.sinri.keel.facade.async;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @since 2.9.4
 */
public class FailedInParallel extends Exception {
    private final @NotNull List<Throwable> causes;

    public FailedInParallel(@NotNull List<Throwable> causes) {
        super("Totally " + causes.size() + " cause(s).");
        this.causes = causes;
    }

    public @NotNull List<Throwable> getCauses() {
        return causes;
    }

    public @Nullable Throwable getCauseAt(int i) {
        return causes.get(i);
    }
}
