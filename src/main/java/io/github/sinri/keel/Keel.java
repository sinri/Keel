package io.github.sinri.keel;

import io.github.sinri.keel.core.helper.*;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.core.logger.KeelLoggerOptions;
import io.github.sinri.keel.core.properties.KeelPropertiesReader;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.github.sinri.keel.mysql.KeelMySQLOptions;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class Keel {
    private static final KeelPropertiesReader propertiesReader = new KeelPropertiesReader();
    private static final Map<String, KeelMySQLKit> mysqlKitMap = new HashMap<>();

    @Deprecated(since = "2.8")
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
     * @param keelVerticle KeelVerticle Instance (deployed)
     * @since 2.2
     */
    @Deprecated(since = "2.8")
    public static void registerDeployedKeelVerticle(KeelVerticle keelVerticle) {
        if (keelVerticle.deploymentID() != null) {
            deployedKeelVerticleMap.put(keelVerticle.deploymentID(), keelVerticle.getVerticleInfo());
        }
    }

    /**
     * @param deploymentID DeploymentID of KeelVerticle Instance (deployed)
     * @since 2.2
     */
    @Deprecated(since = "2.8")
    public static void unregisterDeployedKeelVerticle(String deploymentID) {
        deployedKeelVerticleMap.remove(deploymentID);
    }

    /**
     * @param deploymentID ID of deployment for one Verticle deployed
     * @return the information json object
     * @since 2.2
     */
    @Deprecated(since = "2.8")
    public static JsonObject getDeployedKeelVerticleInfo(String deploymentID) {
        return deployedKeelVerticleMap.get(deploymentID);
    }

    /**
     * @since 2.6
     */
    public static KeelStringHelper stringHelper() {
        return KeelStringHelper.getInstance();
    }

    /**
     * @since 2.6
     */
    public static KeelJsonHelper jsonHelper() {
        return KeelJsonHelper.getInstance();
    }

    /**
     * @since 2.6
     */
    public static KeelFileHelper fileHelper() {
        return KeelFileHelper.getInstance();
    }

    /**
     * @since 2.6
     */
    public static KeelReflectionHelper reflectionHelper() {
        return KeelReflectionHelper.getInstance();
    }

    /**
     * @since 2.6
     */
    public static KeelDateTimeHelper dateTimeHelper() {
        return KeelDateTimeHelper.getInstance();
    }

    public static KeelNetHelper netHelper() {
        return KeelNetHelper.getInstance();
    }
}
