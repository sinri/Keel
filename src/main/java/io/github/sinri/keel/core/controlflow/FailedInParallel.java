package io.github.sinri.keel.core.controlflow;

import java.util.List;

/**
 * @since 2.9.4
 */
public class FailedInParallel extends Exception {
    private final List<Throwable> causes;

    public FailedInParallel(List<Throwable> causes) {
        super("Totally " + causes.size() + " cause(s).");
        this.causes = causes;
    }

    public List<Throwable> getCauses() {
        return causes;
    }

    public Throwable getCauseAt(int i) {
        return causes.get(i);
    }
}
