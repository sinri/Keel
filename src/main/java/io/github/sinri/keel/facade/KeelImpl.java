package io.github.sinri.keel.facade;

import io.github.sinri.keel.mysql.KeelMySQLKitProvider;
import io.vertx.core.*;
import io.vertx.core.spi.cluster.ClusterManager;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class KeelImpl implements Keel {
    private final static KeelMySQLKitProvider mySQLKitProvider = new KeelMySQLKitProvider();
    private static final KeelConfiguration configuration = new KeelConfigurationImpl();
    private static KeelImpl instance;
    private final @Nonnull Vertx vertx;
    private final @Nullable ClusterManager clusterManager;

    private KeelImpl(@Nonnull Vertx vertx, @Nullable ClusterManager clusterManager) {
        this.vertx = vertx;
        this.clusterManager = clusterManager;
    }

    public KeelImpl(Vertx vertx) {
        this(vertx, null);
    }

    public static KeelConfiguration getConfiguration() {
        return configuration;
    }

    public static KeelImpl getInstance() {
        return instance;
    }

    /**
     * 同步启动一个非集群模式的Vertx实例。
     *
     * @param vertxOptions VertxOptions
     * @see <a href="https://vertx.io/docs/apidocs/io/vertx/core/VertxOptions.html">Class VertxOptions</a>
     * @since 2.9.4 针对 2.9.1 的错误弃用进行光复
     */
    public static void initialize(VertxOptions vertxOptions) {
        var vertx = Vertx.vertx(vertxOptions);
        instance = new KeelImpl(vertx);
    }

    /**
     * 异步启动一个Vertx实例，可以为集群模式或非集群模式。
     *
     * @param vertxOptions 如果使用集群模式则必须配置好ClusterManager。
     * @param isClustered  是否使用集群模式
     * @since 2.9.4
     */
    public static Future<Void> initialize(VertxOptions vertxOptions, boolean isClustered) {
        if (isClustered) {
            return Vertx.clusteredVertx(vertxOptions)
                    .compose(vertx -> {
                        instance = new KeelImpl(vertx, vertxOptions.getClusterManager());
                        return Future.succeededFuture();
                    });
        } else {
            initialize(vertxOptions);
            return Future.succeededFuture();
        }
    }

    @Override
    public KeelMySQLKitProvider providerForMySQL() {
        return mySQLKitProvider;
    }

    public @NotNull Vertx getVertx() {
        return vertx;
    }

    public @Nullable ClusterManager getClusterManager() {
        return clusterManager;
    }

    /**
     * @param gracefulHandler what to do before close vertx
     * @since 2.9.4
     */
    public void gracefullyClose(Handler<Promise<Object>> gracefulHandler, Handler<AsyncResult<Void>> vertxCloseHandler) {
        Promise<Object> promise = Promise.promise();
        gracefulHandler.handle(promise);
        promise.future().onComplete(ar -> {
            if (ar.failed()) {
                // todo Keel.outputLogger().exception("Keel.gracefullyClose ERROR, CLOSE ANYWAY", ar.cause());
            } else {
                // todo Keel.outputLogger().notice("Keel.gracefullyClose READY TO CLOSE");
            }
            getVertx().close(vertxCloseHandler);
        });
    }
}
