package io.github.sinri.keel.test;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.mysql.KeelMySQLConfig;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.github.sinri.keel.mysql.jdbc.KeelJDBCForMySQL;
import io.vertx.core.VertxOptions;

public class SharedTestBootstrap {
    private static KeelMySQLKit mySQLKit;
    private static KeelJDBCForMySQL jdbcForMySQL;

    public static void initialize() {
        Keel.initializeVertx(new VertxOptions().setWorkerPoolSize(16));
        Keel.loadPropertiesFromFile("test.properties");

        mySQLKit = new KeelMySQLKit(Keel.getVertx(), new KeelMySQLConfig("local", Keel.getPropertiesReader()));
        jdbcForMySQL = new KeelJDBCForMySQL(new KeelMySQLConfig("local", Keel.getPropertiesReader()));
    }

    public static KeelMySQLKit getMySQLKit() {
        return mySQLKit;
    }

    public static KeelJDBCForMySQL getMySqlJDBC() {
        return jdbcForMySQL;
    }
}
