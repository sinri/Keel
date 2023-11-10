package io.github.sinri.keel.facade.async;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * @since 2.9.4
 */
public class FailedInParallel extends Exception {
    private final @Nonnull List<Throwable> causes;

    public FailedInParallel(@Nonnull List<Throwable> causes) {
        super("Totally " + causes.size() + " cause(s).");
        this.causes = causes;
    }

    public @Nonnull List<Throwable> getCauses() {
        return causes;
    }

    public @Nullable Throwable getCauseAt(int i) {
        return causes.get(i);
    }
}
