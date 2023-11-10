package io.github.sinri.keel.facade;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * It is the facade of Keel, i.e. itself, provides Vertx Instance.
 */
public class Keel {
    private final static KeelConfiguration configuration = KeelConfiguration.createFromJsonObject(new JsonObject());
    private static @Nullable Vertx vertx;
    private static @Nullable ClusterManager clusterManager;

    public static @Nonnull KeelConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * @param dotJoinedKeyChain such as `a.b.c`
     * @since 3.0.1
     */
    public static @Nullable String config(@Nonnull String dotJoinedKeyChain) {
        String[] split = dotJoinedKeyChain.split("\\.");
        return getConfiguration().readString(split);
    }

    public static @Nonnull Vertx getVertx() {
        Objects.requireNonNull(vertx);
        return vertx;
    }

    @Nullable
    public static ClusterManager getClusterManager() {
        return clusterManager;
    }

    public static Future<Void> initializeVertx(@Nonnull VertxOptions vertxOptions) {
        if (vertxOptions.getClusterManager() == null) {
            clusterManager = null;
            vertx = Vertx.vertx(vertxOptions);
            return Future.succeededFuture();
        } else {
            clusterManager = vertxOptions.getClusterManager();
            return Vertx.clusteredVertx(vertxOptions)
                    .compose(v -> {
                        vertx = v;
                        return Future.succeededFuture();
                    });
        }
    }

    /**
     * @since 3.0.1
     */
    public static void initializeVertxStandalone(@Nonnull VertxOptions vertxOptions) {
        if (vertxOptions.getClusterManager() != null) {
            vertxOptions.setClusterManager(null);
        }
        clusterManager = null;
        vertx = Vertx.vertx(vertxOptions);
    }

    public static boolean isVertxInitialized() {
        return vertx != null;
    }

    public static boolean isRunningInVertxCluster() {
        return isVertxInitialized() && getVertx().isClustered();
    }

    /**
     * @since 3.0.10 when running in standalone mode, return empty string instead of null.
     */
    public static @Nonnull String getVertxNodeNetAddress() {
        if (getClusterManager() == null) return "";
        NodeInfo nodeInfo = getClusterManager().getNodeInfo();
        return nodeInfo.host() + ":" + nodeInfo.port();
    }

    /**
     * @since 3.0.10 when running in standalone mode, return empty string instead of null.
     */
    public static @Nonnull String getVertxNodeID() {
        if (getClusterManager() == null) return "";
        return getClusterManager().getNodeId();
    }

    public static Future<Void> gracefullyClose(@Nonnull Handler<Promise<Void>> promiseHandler) {
        Promise<Void> promise = Promise.promise();
        promiseHandler.handle(promise);
        return promise.future().compose(v -> {
            return getVertx().close();
        });
    }

    /**
     * @since 3.0.10
     */
    public static Future<Void> close() {
        return gracefullyClose(Promise::complete);
    }


}
