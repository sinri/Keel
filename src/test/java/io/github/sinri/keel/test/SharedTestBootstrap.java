package io.github.sinri.keel.test;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.mysql.KeelMySQLConfig;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.vertx.core.VertxOptions;

public class SharedTestBootstrap {
    private static KeelMySQLKit mySQLKit;

    public static void initialize() {
        Keel.initializeVertx(new VertxOptions().setWorkerPoolSize(2));
        Keel.loadPropertiesFromFile("test.properties");

        mySQLKit = new KeelMySQLKit(Keel.getVertx(), new KeelMySQLConfig("local", Keel.getPropertiesReader()));
    }

    public static KeelMySQLKit getMySQLKit() {
        return mySQLKit;
    }
}
