package io.github.sinri.keel.test.hazelcast.maxim;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.maids.gatling.KeelGatling;
import io.github.sinri.keel.maids.gatling.KeelGatlingOptions;
import io.vertx.core.Future;
import io.vertx.core.shareddata.Counter;

public class TestGatling {
//    public static List<TestBullet> generateBullets() {
//        List<TestBullet> list = new ArrayList<>();
//
//        for (int i = 0; i < 20; i++) {
//            new TestBullet("Bullet-" + i, i);
//        }
//
//        return list;
//    }

    public static void startOnClusterNode() {
        KeelGatling.deploy(new KeelGatlingOptions("TestGatling")
                .setBulletLoader(() -> Keel.getVertx().sharedData().getCounter("TestGatling")
                        .compose(Counter::incrementAndGet)
                        .compose(i -> Future.succeededFuture(new TestBullet(Math.toIntExact(i)))))
                .setBarrels(3)
                .setAverageRestInterval(1000)
                .setLogger(Keel.outputLogger("Gatling"))
        );
    }
}
