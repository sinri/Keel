package io.github.sinri.keel;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.core.logger.KeelLoggerOptions;
import io.github.sinri.keel.core.properties.KeelPropertiesReader;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.github.sinri.keel.mysql.KeelMySQLOptions;
import io.github.sinri.keel.mysql.jdbc.KeelJDBCForMySQL;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;

import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class Keel {
    private static final String KEY_MYSQL_CONNECTION = "MySQLConnection";
    @Deprecated
    private static final String KEY_JDBC_STATEMENT = "JDBCStatement";
    private static final String KEY_KEEL_LOGGER = "KeelLogger";

    private static final KeelPropertiesReader propertiesReader = new KeelPropertiesReader();
    @Deprecated
    private static final Map<String, KeelLogger> loggerMap = new HashMap<>();
    private static final Map<String, KeelMySQLKit> mysqlKitMap = new HashMap<>();

    @Deprecated
    private static final Map<String, KeelJDBCForMySQL> mysqlKitWithJDBCMap = new HashMap<>();

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
     */
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
        return new KeelLogger(KeelLoggerOptions.generateOptionsForAspectWithPropertiesReader(aspect));
    }

    /**
     * Get a shared KeelLogger instance.
     *
     * @param aspect aspect
     * @return KeelLogger, if already shared, use existed.
     * @deprecated if the logger would be used in a wide scope, use verticle resolution
     */
    @Deprecated
    public static KeelLogger logger(String aspect) {
        if (loggerMap.containsKey(aspect)) {
            return loggerMap.get(aspect);
        }
        KeelLogger logger = standaloneLogger(aspect);
        loggerMap.put(aspect, logger);
        return logger;
    }

    public static KeelLogger outputLogger(String aspect) {
        return new KeelLogger(KeelLoggerOptions.generateOptionsForAspectWithPropertiesReader(aspect).setDir(""));
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
     * @param dataSourceName the data source name
     * @return KeelJDBCForMySQL
     * @since 1.10
     * @deprecated since 2.1
     */
    @Deprecated
    public static KeelJDBCForMySQL getMySQLKitWithJDBC(String dataSourceName) {
        if (!mysqlKitWithJDBCMap.containsKey(dataSourceName)) {
            KeelMySQLOptions keelMySQLOptions = KeelMySQLOptions.generateOptionsForDataSourceWithPropertiesReader(dataSourceName);
            KeelJDBCForMySQL keelJDBCForMySQL = new KeelJDBCForMySQL(keelMySQLOptions);
            mysqlKitWithJDBCMap.put(dataSourceName, keelJDBCForMySQL);
        }
        return mysqlKitWithJDBCMap.get(dataSourceName);
    }

    /**
     * @return getMySQLKitWithJDBC(mysql.default_data_source_name);
     * @since 1.10
     * @deprecated since 2.1
     */
    @Deprecated
    public static KeelJDBCForMySQL getMySQLKitWithJDBC() {
        String defaultName = propertiesReader.getProperty("mysql.default_data_source_name");
        return getMySQLKitWithJDBC(defaultName);
    }

    /**
     * @return
     * @since 2.0
     */
    public static SqlConnection getMySqlConnectionInContext() {
        return Keel.getVertx().getOrCreateContext().get(KEY_MYSQL_CONNECTION);
    }

    /**
     * @param sqlConnection
     * @since 2.0
     */
    public static void setMySqlConnectionInContext(SqlConnection sqlConnection) {
        Keel.getVertx().getOrCreateContext().put(KEY_MYSQL_CONNECTION, sqlConnection);
    }

    /**
     * @return
     * @since 2.0
     * @deprecated since 2.1
     */
    @Deprecated
    public static Statement getJDBCStatementInContext() {
        return Keel.getVertx().getOrCreateContext().get(KEY_JDBC_STATEMENT);
    }

    /**
     * @param statement
     * @since 2.0
     * @deprecated since 2.1
     */
    @Deprecated
    public static void setJDBCStatementInContext(Statement statement) {
        Keel.getVertx().getOrCreateContext().put(KEY_JDBC_STATEMENT, statement);
    }

    /**
     * @return
     * @since 2.0
     */
    public static KeelLogger getKeelLoggerInContext() {
//        System.out.println("Keel::getKeelLoggerInContext as "+Keel.getVertx().getOrCreateContext().deploymentID());
        KeelLogger logger = Keel.getVertx().getOrCreateContext().get(KEY_KEEL_LOGGER);
        if (logger == null) {
            //logger = KeelLogger.buildSilentLogger();
            logger = new KeelLogger();
        }
        return logger;
    }

    /**
     * @param logger
     * @since 2.0
     */
    public static void setKeelLoggerInContext(KeelLogger logger) {
        Keel.getVertx().getOrCreateContext().put(KEY_KEEL_LOGGER, logger);
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
     * @param deploymentID
     * @return
     * @since 2.2
     */
    public static JsonObject getDeployedKeelVerticleInfo(String deploymentID) {
        return deployedKeelVerticleMap.get(deploymentID);
    }
}
