package io.github.sinri.keel.test.hazelcast;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import java.util.List;

public class Cluster {

    public static Future<Void> startCluster() {
        return startClusterOffice();
    }

    public static Future<Void> startClusterOffice() {
        return Keel.initializeClusteredVertx(
                "office",
                List.of(
                        "172.20.12.66", "172.20.12.170"
                ),
                5701,
                1,
                new VertxOptions()
        );
    }

    public static Future<Void> startClusterLocal() {
        return Keel.initializeClusteredVertx(
                "local",
                List.of("127.0.0.1"),
                5701,
                2,
                new VertxOptions()
        );
    }
}
