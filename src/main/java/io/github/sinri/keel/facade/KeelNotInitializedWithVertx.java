package io.github.sinri.keel.facade;

public class KeelNotInitializedWithVertx extends NullPointerException {
    public KeelNotInitializedWithVertx() {
        super("Keel Vertx Instance was not initialized.");
    }
}
