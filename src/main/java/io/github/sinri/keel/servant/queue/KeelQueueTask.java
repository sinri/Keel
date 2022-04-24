package io.github.sinri.keel.servant.queue;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;


public abstract class KeelQueueTask extends KeelVerticle {
    public KeelQueueTask() {
        super();
    }

    abstract public String getTaskReference();

    abstract public String getTaskCategory();

    abstract public boolean isRunSerially();

    abstract protected KeelLogger prepareLogger();

    // as verticle
    public final void start() {
        setKeelLogger(prepareLogger());

        run()
                .recover(throwable -> {
                    getLogger().exception("KeelQueueTask Caught throwable from Method run", throwable);
                    return Future.succeededFuture();
                })
                .eventually(v -> {
                    getLogger().info("KeelQueueTask to undeploy");
                    return undeployMe();
                });
    }

    abstract protected Future<Void> run();
}
