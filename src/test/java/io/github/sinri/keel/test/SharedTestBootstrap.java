package io.github.sinri.keel.test;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.mysql.KeelMySQLKit;
import io.vertx.core.VertxOptions;

import java.util.concurrent.TimeUnit;

public class SharedTestBootstrap {
    private static KeelMySQLKit mySQLKit;

    public static void initialize() {
        Keel.loadPropertiesFromFile("config.properties");
        Keel.initializeVertx(
                new VertxOptions()
                        .setEventLoopPoolSize(4) // default 2 * number of cores on the machine
                        .setWorkerPoolSize(20)//default 20
                        .setMaxWorkerExecuteTime(60_000_000_000L) // 1s;  default 60_000_000_000 ns = 60s
                        .setMaxWorkerExecuteTimeUnit(TimeUnit.NANOSECONDS)
                        .setBlockedThreadCheckInterval(1000L) // default 1000 ms = 1s
                        .setBlockedThreadCheckIntervalUnit(TimeUnit.MILLISECONDS)
                        .setMaxEventLoopExecuteTime(2000000000L)//default 2000000000 ns = 2s
                        .setMaxEventLoopExecuteTimeUnit(TimeUnit.NANOSECONDS)
        );

        mySQLKit = Keel.getMySQLKit("local");
    }

    public static KeelMySQLKit getMySQLKit() {
        return mySQLKit;
    }

}
