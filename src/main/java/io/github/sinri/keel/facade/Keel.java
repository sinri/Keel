package io.github.sinri.keel.facade;

import io.github.sinri.keel.core.helper.TraitForHelpers;
import io.github.sinri.keel.facade.async.TraitForVertxAsync;
import io.github.sinri.keel.facade.interfaces.TraitForClusteredVertx;
import io.github.sinri.keel.facade.interfaces.TraitForVertx;
import io.github.sinri.keel.lagecy.core.logger.KeelLogger;
import io.github.sinri.keel.mysql.KeelMySQLKitProvider;
import io.vertx.core.*;

import java.util.List;

public interface Keel extends TraitForVertx, TraitForClusteredVertx, TraitForVertxAsync, TraitForHelpers {

    static void configureWithPropertiesFile(String propertiesFile) {
        KeelImpl.getConfiguration().loadPropertiesFile(propertiesFile);
    }

    static KeelConfiguration configuration() {
        return KeelImpl.getConfiguration();
    }

    static Keel getInstance() {
        return KeelImpl.getInstance();
    }

    static Vertx vertx() {
        return KeelImpl.getInstance().getVertx();
    }

    /**
     * 同步启动一个非集群模式的Vertx实例。
     *
     * @param vertxOptions VertxOptions
     * @see <a href="https://vertx.io/docs/apidocs/io/vertx/core/VertxOptions.html">Class VertxOptions</a>
     * @since 2.9.4 针对 2.9.1 的错误弃用进行光复
     */
    static void initialize(VertxOptions vertxOptions) {
        KeelImpl.initialize(vertxOptions);
    }

    /**
     * 异步启动一个Vertx实例，可以为集群模式或非集群模式。
     *
     * @param vertxOptions 如果使用集群模式则必须配置好ClusterManager。
     * @param isClustered  是否使用集群模式
     * @since 2.9.4
     */
    static Future<Void> initialize(VertxOptions vertxOptions, boolean isClustered) {
        return KeelImpl.initialize(vertxOptions, isClustered);
    }

    /**
     * 构建一个简易集群。
     * 可以直接用于 SAE on Aliyun.
     *
     * @param clusterName  集群名称
     * @param members      集群组内地址成员
     * @param port         起始端口
     * @param portCount    递增尝试端口数量
     * @param vertxOptions Vert.x 参数
     * @return 未来
     * @since 2.9.1
     */
    static Future<Void> initialize(
            String clusterName,
            List<String> members,
            int port, int portCount,
            VertxOptions vertxOptions
    ) {
        var clusterManager = TraitForClusteredVertx.createClusterManagerForSAE(clusterName, members, port, portCount);
        vertxOptions.setClusterManager(clusterManager);
        return Keel.initialize(vertxOptions, true);
    }


    static void gracefullyClose(Handler<Promise<Object>> gracefulHandler, Handler<AsyncResult<Void>> vertxCloseHandler) {
        KeelImpl.getInstance().gracefullyClose(gracefulHandler, vertxCloseHandler);
    }

    static Future<Void> gracefullyClose(Handler<Promise<Object>> gracefulHandler) {
        Promise<Void> vertxClosed = Promise.promise();
        KeelImpl.getInstance().gracefullyClose(gracefulHandler, vertxClosed);
        return vertxClosed.future();
    }

    /**
     * @since 2.9
     */
    @Deprecated(since = "3.0.0")
    static KeelLogger outputLogger() {
        // todo
        throw new RuntimeException("TODO");
    }

    KeelMySQLKitProvider providerForMySQL();

}
