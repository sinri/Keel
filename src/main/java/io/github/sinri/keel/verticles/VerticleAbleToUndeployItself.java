package io.github.sinri.keel.verticles;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.core.Verticle;

/**
 * @since 1.14
 */
public interface VerticleAbleToUndeployItself extends Verticle {
    static Future<Void> undeploy(String deploymentID) {
        return Keel.getVertx().undeploy(deploymentID);
    }

    Future<Void> undeployMe();
}
