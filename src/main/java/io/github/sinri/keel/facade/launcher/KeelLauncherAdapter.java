package io.github.sinri.keel.facade.launcher;

import io.github.sinri.keel.logger.issue.record.event.RoutineIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.impl.launcher.VertxLifecycleHooks;

import javax.annotation.Nullable;

/**
 * @since 3.0.10 Technical Preview
 * @since 3.0.18 Finished Technical Preview.
 */
public interface KeelLauncherAdapter extends VertxLifecycleHooks {

    /**
     * Run this in main.
     * Do not override this.
     *
     * @param args refer to the main.
     */
    default void launch(String[] args) {
        this.launcher().dispatch(args);
    }

    /**
     * Create a launcher.
     * Do not override this.
     */
    default KeelLauncher launcher() {
        return new KeelLauncher(this);
    }

    /**
     * @since 3.2.0
     */
    KeelIssueRecorder<RoutineIssueRecord> routineIssueRecorder();


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
