package io.github.sinri.keel.test;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.Handler;

import java.util.concurrent.TimeUnit;

public class SharedTestBootstrap {

    public static void bootstrap(Handler<Keel> handler) {
        Keel.genesis(keel -> keel
                                .loadConfigureWithPropertiesFile("config.properties")
                                .compose(configured -> keel.initializeVertx(vertxOptions -> vertxOptions
                                        .setEventLoopPoolSize(4) // default 2 * number of cores on the machine
                                        .setWorkerPoolSize(2)//default 20
                                        .setMaxWorkerExecuteTime(60_000_000_000L) // 1s;  default 60_000_000_000 ns = 60s
                                        .setMaxWorkerExecuteTimeUnit(TimeUnit.NANOSECONDS)
                                        .setBlockedThreadCheckInterval(1000L) // default 1000 ms = 1s
                                        .setBlockedThreadCheckIntervalUnit(TimeUnit.MILLISECONDS)
                                        .setMaxEventLoopExecuteTime(2000000000L)//default 2000000000 ns = 2s
                                        .setMaxEventLoopExecuteTimeUnit(TimeUnit.NANOSECONDS)))
                        //.compose(v -> keel.initializeMySQLDataSource("local"))
                )
                .onSuccess(handler::handle)
                .onFailure(throwable -> {
                    KeelEventLogger.outputLogger().exception(throwable, "Keel Initialize Failure");
                });
    }
}
