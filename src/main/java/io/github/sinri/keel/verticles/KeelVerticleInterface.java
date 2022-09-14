package io.github.sinri.keel.verticles;

import io.github.sinri.keel.Keel;
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
    /**
     * @see KeelVerticleInterface#undeployMe()
     * @deprecated use class method instead
     */
    @Deprecated(since = "2.8")
    static Future<Void> undeploy(String deploymentID) {
        return Keel.getVertx().undeploy(deploymentID);
    }

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
