package io.github.sinri.keel.facade.launcher;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.launcher.VertxLifecycleHooks;

import javax.annotation.Nullable;

/**
 * @since 3.0.10
 */
@TechnicalPreview(since = "3.0.10")
public interface KeelLauncherAdapter extends VertxLifecycleHooks {

    /**
     * Run this in main.
     *
     * @param args refer to the main.
     */
    default void launch(String[] args) {
        this.launcher().dispatch(args);
    }

    /**
     * Create a launcher.
     */
    default KeelLauncher launcher() {
        return new KeelLauncher(this);
    }

    KeelEventLogger logger();


    void beforeStoppingVertx();

    void afterStoppingVertx();

    @Override
    default void handleDeployFailed(Vertx vertx, String mainVerticle, DeploymentOptions deploymentOptions, Throwable cause) {
        vertx.close();
    }

    default @Nullable String getDefaultCommand() {
        return null;
    }

    default @Nullable String getMainVerticle() {
        return null;
    }

}
