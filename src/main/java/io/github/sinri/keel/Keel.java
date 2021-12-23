package io.github.sinri.keel;

import io.github.sinri.keel.core.logger.KeelLogLevel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.core.properties.KeelPropertiesReader;
import io.github.sinri.keel.mysql.KeelMySQLConfig;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Keel {
    private static final KeelPropertiesReader propertiesReader = new KeelPropertiesReader();
    private static final Map<String, KeelLogger> loggerMap = new HashMap<>();
    private static final Map<String, KeelMySQLKit> mysqlKitMap = new HashMap<>();
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

    public static KeelLogger logger(String aspect) {
        if (loggerMap.containsKey(aspect)) {
            return loggerMap.get(aspect);
        }

        String dir = propertiesReader.getProperty(List.of("log", aspect, "dir"));
        //System.out.println("dir -> "+dir);

        KeelLogger logger;
        if (dir == null) {
            logger = new KeelLogger(aspect);
        } else {
            logger = new KeelLogger(new File(dir), aspect);
        }

        String level = propertiesReader.getProperty(List.of("log", aspect, "level"));
        if (level != null) {
            KeelLogLevel lowestLogLevel = KeelLogLevel.valueOf(level);
            //System.out.println("lowestLogLevel -> "+lowestLogLevel);
            logger.setLowestLevel(lowestLogLevel);
        }
        String rotate = propertiesReader.getProperty(List.of("log", aspect, "rotate"));
        if (rotate != null) {
            logger.setRotateDateTimeFormat(rotate);
        }

        loggerMap.put(aspect, logger);
        return logger;
    }

    public static KeelMySQLKit getMySQLKit(String key) {
        if (!mysqlKitMap.containsKey(key)) {
            KeelMySQLConfig config = new KeelMySQLConfig(key, propertiesReader);
            KeelMySQLKit keelMySQLKit = new KeelMySQLKit(Keel.getVertx(), config);
            mysqlKitMap.put(key, keelMySQLKit);
        }
        return mysqlKitMap.get(key);
    }
}
