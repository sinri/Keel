package io.github.sinri.keel.facade;

import io.github.sinri.keel.facade.interfaces.TraitForClusteredVertx;
import io.github.sinri.keel.facade.interfaces.TraitForVertx;
import io.github.sinri.keel.logger.event.KeelEventLogCenter;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.KeelSyncEventLogCenter;
import io.github.sinri.keel.logger.event.adapter.OutputAdapter;
import io.github.sinri.keel.mysql.KeelMySQLConfigure;
import io.github.sinri.keel.mysql.MySQLDataSource;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

class KeelImpl implements Keel, TraitForVertx, TraitForClusteredVertx {
    private final KeelConfiguration configuration;
    private final KeelEventLogCenter outputEventLogCenter;
    private final KeelEventLogger instantEventLogger;
    private final Map<String, MySQLDataSource> mysqlKitMap = new ConcurrentHashMap<>();
    private @Nullable Vertx vertx;
    private @Nullable ClusterManager clusterManager;
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
}
