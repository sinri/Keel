package io.github.sinri.keel.facade.launcher;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

/**
 * @since 3.0.10
 */
@TechnicalPreview
public final class KeelLauncher extends Launcher {

    private final KeelLauncherAdapter adapter;

    public KeelLauncher(@Nonnull KeelLauncherAdapter adapter) {
        this.adapter = adapter;
    }

    private KeelEventLogger logger() {
        return this.adapter.logger();
    }

    @Override
    public void afterConfigParsed(JsonObject config) {
        logger().debug(log -> log
                .message("afterConfigParsed")
                .put("config", config)
        );

        this.adapter.afterConfigParsed(config);
    }

    @Override
    public void beforeStartingVertx(VertxOptions options) {
        logger().debug(log -> log
                .message("beforeStartingVertx")
                .put("VertxOptions", options.toJson())
        );
        this.adapter.beforeStartingVertx(options);
    }

    @Override
    public void afterStartingVertx(Vertx vertx) {
        Keel.setVertx(vertx);
        logger().debug(log -> log
                .message("afterStartingVertx")
        );
        this.adapter.afterStartingVertx(vertx);
    }

    @Override
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
        logger().debug(log -> log
                .message("beforeDeployingVerticle")
                .put("VertxOptions", deploymentOptions.toJson())
        );
        this.adapter.beforeDeployingVerticle(deploymentOptions);
    }

    @Override
    public void handleDeployFailed(Vertx vertx, String mainVerticle, DeploymentOptions deploymentOptions, Throwable cause) {
        this.adapter.handleDeployFailed(vertx, mainVerticle, deploymentOptions, cause);
    }

    @Override
    public void beforeStoppingVertx(Vertx vertx) {
        logger().debug(log -> log
                .message("beforeStoppingVertx")
        );
        this.adapter.beforeStoppingVertx();
    }

    @Override
    public void afterStoppingVertx() {
        logger().debug(log -> log
                .message("afterStoppingVertx")
        );
        this.adapter.afterStoppingVertx();
    }

    @Override
    protected String getDefaultCommand() {
        String defaultCommand = this.adapter.getDefaultCommand();
        if (defaultCommand == null) {
            return super.getDefaultCommand();
        } else {
            return defaultCommand;
        }
    }

    @Override
    protected String getMainVerticle() {
        String mainVerticle = this.adapter.getMainVerticle();
        if (mainVerticle == null) {
            return super.getMainVerticle();
        } else {
            return mainVerticle;
        }
    }


}
