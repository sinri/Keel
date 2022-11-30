package io.github.sinri.keel.servant.sundial;

import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.vertx.core.json.JsonObject;

public class KeelSundialWorkerMeta extends SimpleJsonifiableEntity {
    public KeelSundialWorkerMeta(String name, String cronExpression, int parallelLimit) {
        this.reloadDataFromJsonObject(new JsonObject()
                .put("name", name)
                .put("parallel_limit", parallelLimit)
                .put("cron_expression", cronExpression)
        );
    }

    public String getName() {
        return readString("name");
    }

    public int getParallelLimit() {
        return readInteger("parallel_limit");
    }

    public String getRawCronExpression() {
        return readString("cron_expression");
    }

}
