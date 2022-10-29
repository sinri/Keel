package io.github.sinri.keel.verticles;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * @since 1.14
 */
public interface KeelVerticleInterface extends Verticle {
    KeelLogger getLogger();

    /**
     * @since 2.4 do not rely on context anymore
     * @since 2.7 became public
     */
    void setLogger(KeelLogger logger);

    /**
     * copied from AbstractVerticle
     *
     * @since 2.8
     */
    String deploymentID();

    /**
     * copied from AbstractVerticle
     *
     * @since 2.8
     */
    JsonObject config();

    default JsonObject getVerticleInfo() {
        return new JsonObject()
                .put("class", this.getClass().getName())
                .put("config", this.config())
                .put("deployment_id", this.deploymentID());
    }

    /**
     * @since 2.8
     */
    default Future<String> deployMe(@NotNull Vertx vertx, @NotNull DeploymentOptions deploymentOptions) {
        return vertx.deployVerticle(this, deploymentOptions);
    }

    /**
     * @since 2.8
     */
    default Future<String> deployMe(DeploymentOptions deploymentOptions) {
        return deployMe(Keel.getVertx(), deploymentOptions);
    }

    /**
     * @since 2.8
     */
    default Future<String> deployMe() {
        return deployMe(Keel.getVertx(), new DeploymentOptions());
    }

    /**
     * @since 2.8 add default implementation
     */
    default Future<Void> undeployMe() {
        return getVertx().undeploy(deploymentID());
    }
}
