package io.github.sinri.keel.facade;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.cluster.KeelClusterKit;
import io.github.sinri.keel.helper.KeelHelpersInterface;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * @since 3.1.0
 */
@TechnicalPreview(since = "3.1.0")
public class KeelInstance implements KeelHelpersInterface, KeelClusterKit {
    public static KeelInstance Keel = new KeelInstance();

    private final @Nonnull KeelConfiguration configuration;
    private @Nullable Vertx vertx;
    private @Nullable ClusterManager clusterManager;

    private KeelInstance() {
        this.configuration = KeelConfiguration.createFromJsonObject(new JsonObject());
    }

    @Nonnull
    public KeelConfiguration getConfiguration() {
        return configuration;
    }

    public @Nullable String config(@Nonnull String dotJoinedKeyChain) {
        String[] split = dotJoinedKeyChain.split("\\.");
        return getConfiguration().readString(split);
    }

    public @Nonnull Vertx getVertx() {
        Objects.requireNonNull(vertx);
        return vertx;
    }

    public void setVertx(@Nonnull Vertx outsideVertx) {
        if (vertx == null) {
            vertx = outsideVertx;
        } else {
            throw new IllegalStateException("Vertx Already Initialized");
        }
    }

    @Nullable
    public ClusterManager getClusterManager() {
        return clusterManager;
    }

    public Future<Void> initializeVertx(@Nonnull VertxOptions vertxOptions) {
        return initializeVertx(vertxOptions, null);
    }

    public Future<Void> initializeVertx(
            @Nonnull VertxOptions vertxOptions,
            @Nullable ClusterManager clusterManager
    ) {
        this.clusterManager = clusterManager;
        if (clusterManager == null && vertxOptions.getClusterManager() != null) {
            this.clusterManager = vertxOptions.getClusterManager();
        }
        if (this.clusterManager == null) {
            this.vertx = Vertx.builder().with(vertxOptions).withClusterManager(null).build();
            return Future.succeededFuture();
        } else {
            return Vertx.builder().with(vertxOptions).withClusterManager(clusterManager).buildClustered()
                    .compose(x -> {
                        this.vertx = x;
                        return Future.succeededFuture();
                    });
        }
    }

    public void initializeVertxStandalone(@Nonnull VertxOptions vertxOptions) {
        // todo: remove legacy code, follow vertx
        if (vertxOptions.getClusterManager() != null) {
            vertxOptions.setClusterManager(null);
        }
        this.clusterManager = null;
        this.vertx = Vertx.builder().with(vertxOptions).withClusterManager(null).build();
    }

    public boolean isVertxInitialized() {
        return vertx != null;
    }

    public boolean isRunningInVertxCluster() {
        return isVertxInitialized() && getVertx().isClustered();
    }

    public Future<Void> gracefullyClose(@Nonnull Handler<Promise<Void>> promiseHandler) {
        Promise<Void> promise = Promise.promise();
        promiseHandler.handle(promise);
        return promise.future().compose(v -> {
            return getVertx().close();
        });
    }

    public Future<Void> close() {
        return gracefullyClose(Promise::complete);
    }
}
