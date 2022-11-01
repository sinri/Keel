package io.github.sinri.keel.test.hazelcast;

import com.hazelcast.config.*;
import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import java.util.List;

public class Cluster {

//    private static Vertx vertx;

//    public static Vertx getVertx() {
//        return vertx;
//    }

    public static Future<Void> startCluster(int port) {
//        SharedTestBootstrap.initialize();

        Config hazelcastConfig = ConfigUtil.loadConfig();

        hazelcastConfig
                .setClusterName("h1")
                .setNetworkConfig(new NetworkConfig()
//                                .setPublicAddress("127.0.0.1")
//                                .setPort(port)
                                .setJoin(new JoinConfig()
                                                .setMulticastConfig(new MulticastConfig()
                                                        .setEnabled(false))
                                                .setTcpIpConfig(new TcpIpConfig()
                                                                .setEnabled(true)
//                                                                .addMember("127.0.0.1:14001")
//                                                                .addMember("127.0.0.1:14002")
                                                                .addMember("172.20.12.66")
                                                                .addMember("172.20.12.170")
                                                                .setConnectionTimeoutSeconds(1)
                                                )
                                )
//                                .setInterfaces(new InterfacesConfig()
//                                        .setEnabled(true)
//                                        .addInterface("127.0.0.1")
//                                )
                                .setOutboundPorts(List.of(0))
                );

        ClusterManager mgr = new HazelcastClusterManager(hazelcastConfig);
        VertxOptions options = new VertxOptions().setClusterManager(mgr);

        return Keel.initializeClusteredVertx(hazelcastConfig, options)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        System.out.println("Clustered VertX Installed");

//                        vertx.setPeriodic(1000L,timerID->{
//                            vertx.sharedData()
//                                    .getCounter("c")
//                                    .compose(counter -> {
//                                        return counter.incrementAndGet();
//                                    })
//                                    .onSuccess(x -> {
//                                        System.out.println("x -> " + x);
//                                    })
//                                    .onFailure(throwable -> {
//                                        System.out.println("counter gain error: " + throwable);
//                                    });
//                        });
                    } else {
                        // failed!
                        System.out.println("Clustered VertX Install Failed: " + res.cause());
                    }
                });
    }
}
