package io.github.sinri.keel.facade;

import io.github.sinri.keel.facade.interfaces.KeelTraitForClusteredVertx;
import io.github.sinri.keel.facade.interfaces.KeelTraitForVertx;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.adapter.OutputAdapter;
import io.github.sinri.keel.logger.event.center.KeelSyncEventLogCenter;
import io.github.sinri.keel.mysql.KeelMySQLConfigure;
import io.github.sinri.keel.mysql.MySQLDataSource;
import io.vertx.core.*;
import io.vertx.core.spi.cluster.ClusterManager;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

class KeelImpl implements Keel, KeelTraitForVertx, KeelTraitForClusteredVertx {
    private final KeelConfiguration configuration;
    private final KeelEventLogCenter outputEventLogCenter;
    private final KeelEventLogger instantEventLogger;
    private final Map<String, MySQLDataSource> mysqlKitMap = new ConcurrentHashMap<>();
    private @Nullable Vertx vertx;
    private @Nullable ClusterManager clusterManager;

    public final static AtomicReference<Keel> instanceRef = new AtomicReference<>();

    public KeelImpl() {
        this.configuration = new KeelConfigurationImpl();
        this.outputEventLogCenter = new KeelSyncEventLogCenter(this, OutputAdapter.getInstance());
        this.instantEventLogger = outputEventLogCenter.createLogger("INSTANT");
    }

    public KeelConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    public KeelEventLogger getInstantEventLogger() {
        return this.instantEventLogger;
    }

    @Override
    public KeelEventLogger createOutputEventLogger(String topic) {
        return this.outputEventLogCenter.createLogger(topic);
    }

    public Future<Void> initializeVertx(VertxOptions vertxOptions) {
        boolean isClustered = (vertxOptions.getClusterManager() != null);
        clusterManager = vertxOptions.getClusterManager();
        if (isClustered) {
            return Vertx.clusteredVertx(vertxOptions)
                    .compose(vertxGenerated -> {
                        vertx = vertxGenerated;
                        return Future.succeededFuture();
                    });
        } else {
            vertx = Vertx.vertx(vertxOptions);
            return Future.succeededFuture();
        }
    }

    private final List<Handler<Promise<Void>>> closePrepareHandlers = new ArrayList<>();

    @Override
    public void addClosePrepareHandler(@NotNull Handler<Promise<Void>> closePrepareHandler) {
        closePrepareHandlers.add(closePrepareHandler);
    }

    @NotNull
    @Override
    public List<Handler<Promise<Void>>> getClosePrepareHandlers() {
        return closePrepareHandlers;
    }

    public @NotNull Vertx getVertx() {
        Objects.requireNonNull(vertx);
        return vertx;
    }

    public @Nullable ClusterManager getClusterManager() {
        return clusterManager;
    }

    @Override
    public boolean isVertxInitialized() {
        return vertx != null;
    }

    @Override
    public boolean isRunningInVertxCluster() {
        return clusterManager != null;
    }

    public Future<Void> initializeMySQLDataSource(@Nonnull String dataSourceName) {
        if (!mysqlKitMap.containsKey(dataSourceName)) {
            KeelConfiguration configuration = getConfiguration().extract("mysql", dataSourceName);
            Objects.requireNonNull(configuration);
            KeelMySQLConfigure mySQLConfigure = new KeelMySQLConfigure(dataSourceName, configuration);
            MySQLDataSource mySQLDataSource = new MySQLDataSource(this, mySQLConfigure);
            mysqlKitMap.put(dataSourceName, mySQLDataSource);
        }
        return Future.succeededFuture();
    }

    public MySQLDataSource getMySQLDataSource(@Nonnull String dataSourceName) {
        return mysqlKitMap.get(dataSourceName);
    }

    @NotNull
    @Override
    public String defaultMySQLDataSourceName() {
        return Objects.requireNonNullElse(getConfiguration().readString("mysql", "default_data_source_name"), "default");
    }

    public void gracefullyClose(Handler<Promise<Object>> gracefulHandler, Handler<AsyncResult<Void>> vertxCloseHandler) {
        Promise<Object> promise = Promise.promise();
        List<Handler<Promise<Void>>> closePrepareHandlers = getClosePrepareHandlers();
        this.parallelForAllComplete(closePrepareHandlers, closePrepareHandler -> {
                    Promise<Void> sub_promise = Promise.promise();
                    closePrepareHandler.handle(sub_promise);
                    return sub_promise.future();
                })
                .andThen(closePrepareHandlersOver -> {
                    gracefulHandler.handle(promise);
                    promise.future().onComplete(ar -> {
                        if (ar.failed()) {
                            getInstantEventLogger().exception(ar.cause(), "Keel.gracefullyClose ERROR, CLOSE ANYWAY");
                        } else {
                            getInstantEventLogger().notice("Keel.gracefullyClose READY TO CLOSE");
                        }
                        getVertx().close(vertxCloseHandler);
                    });
                });
    }
}
