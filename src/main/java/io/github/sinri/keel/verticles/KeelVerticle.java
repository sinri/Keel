package io.github.sinri.keel.verticles;

import io.github.sinri.keel.logger.issue.record.KeelIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 1.14
 */
public interface KeelVerticle<T extends KeelIssueRecord<T>> extends Verticle {

    /**
     * @since 3.2.0
     */
    @Nonnull
    KeelIssueRecorder<T> getRoutineIssueRecorder();

    /**
     * @since 3.2.0
     */
    void setRoutineIssueRecorder(@Nonnull KeelIssueRecorder<T> routineIssueRecorder);

    /**
     * copied from AbstractVerticle
     *
     * @since 2.8
     */
    String deploymentID();

    /**
     * copied from AbstractVerticle
     *
     * @see AbstractVerticle
     * @since 2.8
     */
    JsonObject config();

    default JsonObject getVerticleInfo() {
        return new JsonObject()
                .put("class", this.getClass().getName())
                .put("config", this.config())
                .put("deployment_id", this.deploymentID());
    }


    default Future<String> deployMe(DeploymentOptions deploymentOptions) {
        return Keel.getVertx().deployVerticle(this, deploymentOptions);
    }

    /**
     * @since 2.8 add default implementation
     */
    default Future<Void> undeployMe() {
        return Keel.getVertx().undeploy(deploymentID());
    }

}
