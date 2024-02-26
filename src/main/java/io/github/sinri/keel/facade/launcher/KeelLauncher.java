package io.github.sinri.keel.facade.launcher;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.10 Technical Preview
 * @since 3.0.18 Finished Technical Preview.
 */
public final class KeelLauncher extends Launcher {

    private final KeelLauncherAdapter adapter;
    private final KeelEventLogger logger;

    public KeelLauncher(@Nonnull KeelLauncherAdapter adapter) {
        this.adapter = adapter;
        this.logger = adapter.buildEventLoggerForLauncher();
    }

    /**
     * @since 3.2.0
     */
    @Nonnull
    private KeelEventLogger eventLogger() {
        return this.logger;
    }

    @Override
    public void afterConfigParsed(JsonObject config) {
        eventLogger().debug(log -> log
                .message("afterConfigParsed")
                .context(c -> c
                        .put("config", config)
                )
        );

        this.adapter.afterConfigParsed(config);
    }

    @Override
    public void beforeStartingVertx(VertxOptions options) {
        eventLogger().debug(log -> log
                .message("beforeStartingVertx")
                .context(c -> c
                        .put("VertxOptions", options.toJson())
                )
        );
        this.adapter.beforeStartingVertx(options);
    }

    @Override
    public void afterStartingVertx(Vertx vertx) {
        Keel.setVertx(vertx);
        eventLogger().debug(log -> log
                .message("afterStartingVertx")
        );
        this.adapter.afterStartingVertx(vertx);
    }

    @Override
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
        eventLogger().debug(log -> log
                .message("beforeDeployingVerticle")
                .context(c -> c
                        .put("VertxOptions", deploymentOptions.toJson())
                )
        );
        this.adapter.beforeDeployingVerticle(deploymentOptions);
    }

    @Override
    public void handleDeployFailed(Vertx vertx, String mainVerticle, DeploymentOptions deploymentOptions, Throwable cause) {
        this.adapter.handleDeployFailed(vertx, mainVerticle, deploymentOptions, cause);
    }

    @Override
    public void beforeStoppingVertx(Vertx vertx) {
        eventLogger().debug(log -> log
                .message("beforeStoppingVertx")
        );
        this.adapter.beforeStoppingVertx();
    }

    @Override
    public void afterStoppingVertx() {
        eventLogger().debug(log -> log
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
