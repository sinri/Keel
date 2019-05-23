package io.github.sinri.Keel;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import java.util.logging.Logger;

public class Keel {
    protected static Vertx sharedVertX = null;
    protected static VertxOptions sharedVertXOptions;

    public static Vertx getVertX() {
        if (sharedVertX == null) {
            initializeVertX();
        }
        return sharedVertX;
    }

    public static void setSharedVertXOptions(VertxOptions options) {
        sharedVertXOptions = options;
    }

    private static void initializeVertX() {
        if (sharedVertX != null) {
            sharedVertX.close(r -> {
                // log close done
            });
        }
        if (sharedVertXOptions == null) {
            sharedVertXOptions = new VertxOptions();
        }

        sharedVertX = Vertx.vertx(sharedVertXOptions);
    }

    //protected static Map<String, Logger> loggerMap;

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.toString());
    }

    public static Logger getLogger(String clazz) {
        Logger logger;
        logger = java.util.logging.Logger.getLogger(clazz);
        return logger;
    }

}
