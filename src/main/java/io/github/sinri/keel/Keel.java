package io.github.sinri.keel;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.core.logger.KeelLoggerOptions;
import io.github.sinri.keel.core.properties.KeelPropertiesReader;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.github.sinri.keel.mysql.KeelMySQLOptions;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;

import java.util.HashMap;
import java.util.Map;

public class Keel {
    private static final String KEY_MYSQL_CONNECTION = "MySQLConnection";
    private static final String KEY_KEEL_LOGGER = "KeelLogger";

    private static final KeelPropertiesReader propertiesReader = new KeelPropertiesReader();
    @Deprecated(since = "2.2", forRemoval = true)
    private static final Map<String, KeelLogger> loggerMap = new HashMap<>();
    private static final Map<String, KeelMySQLKit> mysqlKitMap = new HashMap<>();

    private static final Map<String, JsonObject> deployedKeelVerticleMap = new HashMap<>();

    private static Vertx vertx;

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
    public static void initializeVertx(VertxOptions vertxOptions) {
        vertx = Vertx.vertx(vertxOptions);
    }

    public static Vertx getVertx() {
        if (vertx == null) {
            throw new RuntimeException("The shared vertx instance was not initialized. Run `Keel.initializeVertx()` first!");
        }
        return vertx;
    }

    /**
     * @since 2.1
     * @deprecated since 2.3
     */
    @Deprecated(since = "2.3", forRemoval = true)
    public static void closeVertx() {
        if (vertx != null) {
            vertx.close();
        }
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
                .loadForAspect(aspect);
        return new KeelLogger(options);
    }

    /**
     * Get a shared KeelLogger instance.
     *
     * @param aspect aspect
     * @return KeelLogger, if already shared, use existed.
     * @deprecated if the logger would be used in a wide scope, use verticle resolution
     */
    @Deprecated(since = "2.2", forRemoval = true)
    public static KeelLogger logger(String aspect) {
        if (loggerMap.containsKey(aspect)) {
            return loggerMap.get(aspect);
        }
        KeelLogger logger = standaloneLogger(aspect);
        loggerMap.put(aspect, logger);
        return logger;
    }

    public static KeelLogger outputLogger(String aspect) {
        KeelLoggerOptions options = new KeelLoggerOptions()
                .setCompositionStyle(KeelLoggerOptions.CompositionStyle.THREE_LINES)
                .loadForAspect(aspect)
                .setDir("");
        return new KeelLogger(options);
    }

    public static KeelMySQLKit getMySQLKit(String dataSourceName) {
        if (!mysqlKitMap.containsKey(dataSourceName)) {
            KeelMySQLOptions keelMySQLOptions = KeelMySQLOptions.generateOptionsForDataSourceWithPropertiesReader(dataSourceName);
            KeelMySQLKit keelMySQLKit = new KeelMySQLKit(Keel.getVertx(), keelMySQLOptions);
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
     * @since 2.0
     * @deprecated since 2.3
     */
    @Deprecated(since = "2.3", forRemoval = true)
    public static SqlConnection getMySqlConnectionInContext() {
        return Keel.getVertx().getOrCreateContext().get(KEY_MYSQL_CONNECTION);
    }

    /**
     * @since 2.0
     * @deprecated since 2.3
     */
    @Deprecated(since = "2.3", forRemoval = true)
    public static void setMySqlConnectionInContext(SqlConnection sqlConnection) {
        Keel.getVertx().getOrCreateContext().put(KEY_MYSQL_CONNECTION, sqlConnection);
    }

    /**
     * @since 2.3
     */
    @Deprecated(since = "2.4")
    public static SqlConnection getMySqlConnectionInContext(Context context) {
        return context.get(KEY_MYSQL_CONNECTION);
    }

    /**
     * @since 2.3
     */
    @Deprecated(since = "2.4")
    public static void setMySqlConnectionInContext(Context context, SqlConnection sqlConnection) {
        context.put(KEY_MYSQL_CONNECTION, sqlConnection);
    }

    /**
     * @since 2.0
     * @deprecated since 2.3
     */
    @Deprecated(since = "2.3", forRemoval = true)
    public static KeelLogger getKeelLoggerInContext() {
        return getKeelLoggerInContext(Keel.getVertx().getOrCreateContext());
    }

    /**
     * @since 2.2
     */
    @Deprecated(since = "2.4")
    public static KeelLogger getKeelLoggerInContext(Context context) {
        KeelLogger logger = context.get(KEY_KEEL_LOGGER);
        if (logger == null) {
            logger = new KeelLogger();
        }
        return logger;
    }

    /**
     * @since 2.0
     * @deprecated since 2.3
     */
    @Deprecated(since = "2.3", forRemoval = true)
    public static void setKeelLoggerInContext(KeelLogger logger) {
        setKeelLoggerInContext(Keel.getVertx().getOrCreateContext(), logger);
    }

    /**
     * @since 2.2
     */
    @Deprecated(since = "2.4")
    public static void setKeelLoggerInContext(Context context, KeelLogger logger) {
        context.put(KEY_KEEL_LOGGER, logger);
    }

    /**
     * @param keelVerticle KeelVerticle Instance (deployed)
     * @since 2.2
     */
    public static void registerDeployedKeelVerticle(KeelVerticle keelVerticle) {
        if (keelVerticle.deploymentID() != null) {
            deployedKeelVerticleMap.put(keelVerticle.deploymentID(), keelVerticle.getVerticleInfo());
        }
    }

    /**
     * @param deploymentID DeploymentID of KeelVerticle Instance (deployed)
     * @since 2.2
     */
    public static void unregisterDeployedKeelVerticle(String deploymentID) {
        deployedKeelVerticleMap.remove(deploymentID);
    }

    /**
     * @param deploymentID ID of deployment for one Verticle deployed
     * @return the information json object
     * @since 2.2
     */
    public static JsonObject getDeployedKeelVerticleInfo(String deploymentID) {
        return deployedKeelVerticleMap.get(deploymentID);
    }
}
