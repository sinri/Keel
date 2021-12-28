package io.github.sinri.keel.servant;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public abstract class KeelServantQueueTask {
    public final String EPITAPH_DONE = "DONE";
    public final String EPITAPH_ERROR = "ERROR";

    abstract public String getTaskReference();

    /**
     * Do any lock action here, and tell the next whether succeeded
     *
     * @return A future with true or false, never  return failed future
     */
    public Future<Boolean> lockTask() {
        return Future.succeededFuture(true);
    }

    /**
     * @return the success future with feedback, or failed future
     */
    abstract public Future<String> execute();

    public KeelLogger getLogger() {
        return new KeelLogger();
    }

    public final Future<Void> finalExecute() {
        return lockTask()
                .compose(locked -> {
                    if (!locked) {
                        getLogger().warning(getClass() + " [" + getTaskReference() + "] LOCK FAILED");
                        return Future.failedFuture("LOCKED FAILED FOR [" + getTaskReference() + "] " + getClass());
                    } else {
                        return execute()
                                .compose(feedback -> markTaskAsCompleted(this.EPITAPH_DONE, feedback))
                                .recover(throwable -> {
                                    getLogger().error(getClass() + " [" + getTaskReference() + "] EXECUTE FAILED: " + throwable.getMessage());
                                    getLogger().exception(throwable);
                                    return markTaskAsCompleted(this.EPITAPH_ERROR, throwable.getMessage());
                                });
                    }
                });
    }

    public Future<Void> markTaskAsCompleted(String epitaph, String feedback) {
        getLogger().notice(getClass() + "[" + getTaskReference() + "] mark as " + epitaph, new JsonObject().put("feedback", feedback));
        return Future.succeededFuture();
    }
}
