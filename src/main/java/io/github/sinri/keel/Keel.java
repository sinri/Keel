package io.github.sinri.keel;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.core.logger.KeelLoggerOptions;
import io.github.sinri.keel.core.properties.KeelPropertiesReader;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.github.sinri.keel.mysql.KeelMySQLOptions;
import io.github.sinri.keel.mysql.jdbc.KeelJDBCForMySQL;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

public class Keel {
    private static final KeelPropertiesReader propertiesReader = new KeelPropertiesReader();
    private static final Map<String, KeelLogger> loggerMap = new HashMap<>();
    private static final Map<String, KeelMySQLKit> mysqlKitMap = new HashMap<>();
    private static final Map<String, KeelJDBCForMySQL> mysqlKitWithJDBCMap = new HashMap<>();
    private static Vertx vertx;

    public static void loadPropertiesFromFile(String propertiesFileName) {
        propertiesReader.appendPropertiesFromFile(propertiesFileName);
    }

    public static KeelPropertiesReader getPropertiesReader() {
        return propertiesReader;
    }

    public static void initializeVertx(VertxOptions vertxOptions) {
        vertx = Vertx.vertx(vertxOptions);
    }

    public static Vertx getVertx() {
        if (vertx == null) {
            throw new RuntimeException("The shared vertx instance was not initialized. Run `Keel.initializeVertx()` first!");
        }
        return vertx;
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
     */
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
     */
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
     */
    public static KeelJDBCForMySQL getMySQLKitWithJDBC() {
        String defaultName = propertiesReader.getProperty("mysql.default_data_source_name");
        return getMySQLKitWithJDBC(defaultName);
    }

}
