package io.github.sinri.keel.servant;

import io.vertx.core.Future;

public abstract class KeelServantQueueTask {
    abstract public String getTaskReference();

    abstract public Future<String> execute();
}
