package io.github.sinri.keel.core.controlflow;

/**
 * @since 2.9.2
 */
public class PromiseTimeout extends Exception {
    public PromiseTimeout(long t) {
        super("WAITED " + t + " ms FOR PROMISE");
    }
}
