package io.github.sinri.keel.facade;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeInfo;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * It is the facade of Keel, i.e. itself, provides Vertx Instance.
 */
public class Keel {
    private final static KeelConfiguration configuration = KeelConfiguration.createFromJsonObject(new JsonObject());
    private static @Nullable Vertx vertx;
    private static @Nullable ClusterManager clusterManager;

    public static KeelConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * @param dotJoinedKeyChain such as `a.b.c`
     * @since 3.0.1
     */
    public static String config(@NotNull String dotJoinedKeyChain) {
        String[] split = dotJoinedKeyChain.split("\\.");
        return getConfiguration().readString(split);
    }

    public static Vertx getVertx() {
        Objects.requireNonNull(vertx);
        return vertx;
    }

    @Nullable
    public static ClusterManager getClusterManager() {
        return clusterManager;
    }

    public static Future<Void> initializeVertx(VertxOptions vertxOptions) {
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
    public static void initializeVertxStandalone(VertxOptions vertxOptions) {
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

    public static String getVertxNodeNetAddress() {
        if (getClusterManager() == null) return null;
        NodeInfo nodeInfo = getClusterManager().getNodeInfo();
        return nodeInfo.host() + ":" + nodeInfo.port();
    }

    public static String getVertxNodeID() {
        if (getClusterManager() == null) return null;
        return getClusterManager().getNodeId();
    }

    public static Future<Void> gracefullyClose(Handler<Promise<Void>> promiseHandler) {
        Promise<Void> promise = Promise.promise();
        promiseHandler.handle(promise);
        return promise.future().compose(v -> {
            return getVertx().close();
        });
    }

}
