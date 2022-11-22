package io.github.sinri.keel;

import com.hazelcast.config.*;
import io.github.sinri.keel.core.controlflow.FutureForEach;
import io.github.sinri.keel.core.controlflow.FutureForRange;
import io.github.sinri.keel.core.controlflow.FutureSleep;
import io.github.sinri.keel.core.controlflow.FutureUntil;
import io.github.sinri.keel.core.helper.KeelHelpers;
import io.github.sinri.keel.core.logger.KeelLogLevel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.core.logger.KeelLoggerOptions;
import io.github.sinri.keel.core.properties.KeelPropertiesReader;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.github.sinri.keel.mysql.KeelMySQLOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeInfo;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class Keel {
    private static final KeelPropertiesReader propertiesReader = new KeelPropertiesReader();
    private static final Map<String, KeelMySQLKit> mysqlKitMap = new HashMap<>();

    private static Vertx vertx;
    private static ClusterManager clusterManager;

    public static void loadPropertiesFromFile(String propertiesFileName) {
        propertiesReader.appendPropertiesFromFile(propertiesFileName);
    }

    public static KeelPropertiesReader getPropertiesReader() {
        return propertiesReader;
    }

    /**
     * @param vertxOptions VertxOptions
     * @see <a href="https://vertx.io/docs/apidocs/io/vertx/core/VertxOptions.html">Class VertxOptions</a>
     */
    @Deprecated(since = "2.9.1")
    public static void initializeVertx(VertxOptions vertxOptions) {
        initializeStandaloneVertx(vertxOptions);
    }

    /**
     * @since 2.9.1
     */
    public static Future<Void> initializeStandaloneVertx(VertxOptions vertxOptions) {
        vertx = Vertx.vertx(vertxOptions);
        return Future.succeededFuture();
    }

    /**
     * 构建一个简易集群 for SAE on Aliyun.
     *
     * @param clusterName  集群名称
     * @param members      集群组内地址成员
     * @param port         起始端口
     * @param portCount    递增尝试端口数量
     * @param vertxOptions Vert.x 参数
     * @return 未来
     * @since 2.9.1
     */
    public static Future<Void> initializeClusteredVertx(
            String clusterName,
            List<String> members,
            int port, int portCount,
            VertxOptions vertxOptions
    ) {
        TcpIpConfig tcpIpConfig = new TcpIpConfig()
                .setEnabled(true)
                .setConnectionTimeoutSeconds(1);
        members.forEach(tcpIpConfig::addMember);

        JoinConfig joinConfig = new JoinConfig()
                .setMulticastConfig(new MulticastConfig().setEnabled(false))
                .setTcpIpConfig(tcpIpConfig);

        NetworkConfig networkConfig = new NetworkConfig()
                .setJoin(joinConfig)
                .setPort(port)
                .setPortCount(portCount)
                .setPortAutoIncrement(portCount > 1)
                .setOutboundPorts(List.of(0));

        Config hazelcastConfig = ConfigUtil.loadConfig()
                .setClusterName(clusterName)
                .setNetworkConfig(networkConfig);

        clusterManager = new HazelcastClusterManager(hazelcastConfig);
        vertxOptions.setClusterManager(clusterManager);

        return initializeClusteredVertx(vertxOptions);
    }

    /**
     * 构建一个简易集群 for SAE on Aliyun.
     *
     * @param clusterName  集群名称
     * @param members      集群组内地址成员
     * @param vertxOptions Vert.x 参数
     * @return 未来
     * @since 2.9.1
     */
    public static Future<Void> initializeClusteredVertx(
            String clusterName,
            List<String> members,
            VertxOptions vertxOptions
    ) {
        return initializeClusteredVertx(clusterName, members, 5701, 1, vertxOptions);
    }

    /**
     * 以程序编辑的方式构建一个集群。
     *
     * @param vertxOptions Vert.x 选项
     * @return 未来
     * @since 2.9
     */
    public static Future<Void> initializeClusteredVertx(VertxOptions vertxOptions) {
        // Config hazelcastConfig;// Hazelcast 配置
        //ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);
        //VertxOptions options = new VertxOptions().setClusterManager(mgr);
        clusterManager = vertxOptions.getClusterManager();
        return Vertx.clusteredVertx(vertxOptions)
                .compose(clusteredVertx -> {
                    vertx = clusteredVertx;
                    return Future.succeededFuture();
                });
    }

    public static Vertx getVertx() {
        if (vertx == null) {
            throw new RuntimeException("The shared vertx instance was not initialized. Run `Keel.initializeVertx()` first!");
        }
        return vertx;
    }

    public static ClusterManager getVertxClusterManager() {
        return clusterManager;
    }

    public static String getVertxNodeNetAddress() {
        if (vertx == null || clusterManager == null) return null;
        NodeInfo nodeInfo = clusterManager.getNodeInfo();
        return nodeInfo.host() + ":" + nodeInfo.port();
    }

    public static String getVertxNodeID() {
        if (vertx == null || clusterManager == null) return null;
        return clusterManager.getNodeId();
    }

    public static EventBus getEventBus() {
        return vertx.eventBus();
    }

    /**
     * @param aspect the aspect
     * @return a new KeelLogger instance (would not be shared)
     * @since 1.11
     */
    public static KeelLogger standaloneLogger(String aspect) {
        KeelLoggerOptions options = new KeelLoggerOptions()
                .addIgnorableStackPackage("io.vertx,io.netty,java.lang")
                .loadForAspect(aspect);
        return KeelLogger.createLogger(options);
    }

    public static KeelLogger outputLogger(String aspect) {
        KeelLoggerOptions options = new KeelLoggerOptions()
                .setCompositionStyle(KeelLoggerOptions.CompositionStyle.THREE_LINES)
                .addIgnorableStackPackage("io.vertx,io.netty,java.lang")
                .loadForAspect(aspect)
                .setImplement("print");
        return KeelLogger.createLogger(options);
    }

    /**
     * This method to get output logger, with all options could be overwritten.
     *
     * @since 2.9
     */
    public static KeelLogger outputLogger(String aspect, Handler<KeelLoggerOptions> optionsHandler) {
        KeelLoggerOptions options = new KeelLoggerOptions()
                .setCompositionStyle(KeelLoggerOptions.CompositionStyle.THREE_LINES)
                .addIgnorableStackPackage("io.vertx,io.netty,java.lang")
                .loadForAspect(aspect)
                .setImplement("print");
        if (optionsHandler != null) optionsHandler.handle(options);
        return KeelLogger.createLogger(options);
    }

    /**
     * @since 2.9
     */
    public static KeelLogger outputLogger(Handler<KeelLoggerOptions> optionsHandler) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 3) {
            return outputLogger("unknown");
        } else {
            StackTraceElement st = stackTrace[2];
            return outputLogger(st.getClassName() + "::" + st.getMethodName(), optionsHandler)
                    .setCategoryPrefix("{" + st.getFileName() + ":" + st.getLineNumber() + "}");
        }
    }

    /**
     * @since 2.9
     */
    public static KeelLogger outputLogger() {
        String clusterNode = "";
        if (vertx != null && vertx.isClustered()) {
//            clusterNode = getVertxNodeID()+"|";
            clusterNode = getVertxNodeNetAddress() + "|";
        }

        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 3) {
            return outputLogger(clusterNode + "unknown", keelLoggerOptions -> keelLoggerOptions.setLowestVisibleLogLevel(KeelLogLevel.DEBUG));
        } else {
            StackTraceElement st = stackTrace[2];
            return outputLogger(clusterNode + st.getClassName(), keelLoggerOptions -> keelLoggerOptions.setLowestVisibleLogLevel(KeelLogLevel.DEBUG))
                    .setContentPrefix(st.toString());
        }
    }

    public static KeelMySQLKit getMySQLKit(String dataSourceName) {
        if (!mysqlKitMap.containsKey(dataSourceName)) {
            KeelMySQLOptions keelMySQLOptions = KeelMySQLOptions.generateOptionsForDataSourceWithPropertiesReader(dataSourceName);
            KeelMySQLKit keelMySQLKit = new KeelMySQLKit(keelMySQLOptions);
            mysqlKitMap.put(dataSourceName, keelMySQLKit);
        }
        return mysqlKitMap.get(dataSourceName);
    }

    /**
     * @return getMySQLKit(mysql.default_data_source_name);
     * @since 1.10
     */
    public static KeelMySQLKit getMySQLKit() {
        String defaultName = propertiesReader.getProperty("mysql.default_data_source_name");
        return getMySQLKit(defaultName);
    }

    /**
     * @since 2.9
     */
    public static KeelHelpers helpers() {
        return KeelHelpers.getInstance();
    }

    /**
     * @since 2.9
     */
    public static <R> Future<R> executeWithinLock(String lockName, Supplier<Future<R>> supplier) {
        return executeWithinLock(lockName, 10_000L, supplier);
    }

    /**
     * @since 2.9
     */
    public static <R> Future<R> executeWithinLock(String lockName, long timeout, Supplier<Future<R>> supplier) {
        return getVertx().sharedData().getLockWithTimeout(lockName, timeout)
                .compose(lock -> Future.succeededFuture()
                        .compose(v -> supplier.get())
                        .onComplete(ar -> lock.release()));
    }

    /**
     * @since 2.9
     */
    public static Future<Void> callFutureUntil(Supplier<Future<Boolean>> singleRecursionForShouldStopSupplier) {
        return FutureUntil.call(singleRecursionForShouldStopSupplier);
    }

    /**
     * @since 2.9
     */
    public static <T> Future<Void> callFutureForEach(Iterable<T> collection, Function<T, Future<Void>> itemProcessor) {
        return FutureForEach.call(collection, itemProcessor);
    }

    /**
     * @since 2.9
     */
    public static Future<Void> callFutureForRange(FutureForRange.Options options, Function<Integer, Future<Void>> handleFunction) {
        return FutureForRange.call(options, handleFunction);
    }

    /**
     * @since 2.9
     */
    public static Future<Void> callFutureForRange(int times, Function<Integer, Future<Void>> handleFunction) {
        return FutureForRange.call(times, handleFunction);
    }

    /**
     * @since 2.9
     */
    public static Future<Void> callFutureSleep(long t) {
        return FutureSleep.call(t);
    }
}
