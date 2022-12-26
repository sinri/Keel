package io.github.sinri.keel.verticles;

import io.github.sinri.keel.facade.Keel3;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonObject;

/**
 * @since 1.14
 */
public interface KeelVerticle extends Verticle {

    KeelEventLogger getLogger();

    /**
     * @since 2.4 do not rely on context anymore
     * @since 2.7 became public
     * @since 2.9.3 become optional with nothing to do
     */
    void setLogger(KeelEventLogger logger);

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
        return Keel3.getVertx().deployVerticle(this, deploymentOptions);
    }

    /**
     * @since 2.8 add default implementation
     */
    default Future<Void> undeployMe() {
        return Keel3.getVertx().undeploy(deploymentID());
    }

}
