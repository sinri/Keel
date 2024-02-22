package io.github.sinri.keel.test.lab.launcher;

import io.github.sinri.keel.logger.event.legacy.KeelEventLogger;
import io.github.sinri.keel.logger.event.legacy.center.KeelOutputEventLogCenter;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Launcher;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;

public class TestKeelVertxLauncher extends Launcher {

    private final KeelEventLogger logger;

    public TestKeelVertxLauncher() {
        this.logger = KeelOutputEventLogCenter.getInstance().createLogger("launcher");
    }

    @Override
    public void afterConfigParsed(JsonObject config) {
        logger.info(log -> log
                .message("TestKeelVertxLauncher::afterConfigParsed")
                .context(c -> c
                        .put("config", config)
                )
        );
    }

    @Override
    public void beforeStartingVertx(VertxOptions options) {
        logger.info(log -> log
                .message("TestKeelVertxLauncher::beforeStartingVertx")
                .context(c -> c
                        .put("VertxOptions", options.toJson())
                )
        );
    }


    @Override
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
        logger.info(log -> log
                .message("TestKeelVertxLauncher::beforeDeployingVerticle")
                .context(c -> c
                        .put("VertxOptions", deploymentOptions.toJson())
                )
        );
    }

    @Override
    public void afterStartingVertx(Vertx vertx) {
        logger.info(log -> log
                .message("TestKeelVertxLauncher::afterStartingVertx")
        );
    }

    @Override
    public void beforeStoppingVertx(Vertx vertx) {
        logger.info(log -> log
                .message("TestKeelVertxLauncher::beforeStoppingVertx")
        );
    }

    @Override
    public void afterStoppingVertx() {
        logger.info(log -> log
                .message("TestKeelVertxLauncher::afterStoppingVertx")
        );
    }

    @Override
    protected String getDefaultCommand() {
        return "run";
    }

    @Override
    protected String getMainVerticle() {
        return TestMainVerticle.class.getName();
    }
}
