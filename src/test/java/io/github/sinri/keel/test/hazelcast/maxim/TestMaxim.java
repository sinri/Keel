package io.github.sinri.keel.test.hazelcast.maxim;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.maids.maxim.Bullet;
import io.github.sinri.keel.maids.maxim.KeelMaxim;
import io.vertx.core.Future;

import java.util.function.Supplier;

public class TestMaxim {
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
        new KeelMaxim()
                .setLogger(Keel.outputLogger("Maxim"))
                .setBarrels(3)
                .setAverageRestInterval(1000)
                .setBulletLoader(new Supplier<Future<Bullet>>() {
                    @Override
                    public Future<Bullet> get() {

                        return Keel.getVertx().sharedData().getCounter("Maxim")
                                .compose(counter -> {
                                    return counter.incrementAndGet();
                                })
                                .compose(i -> {
                                    TestBullet bullet = new TestBullet(Math.toIntExact(i));
                                    return Future.succeededFuture(bullet);
                                });
                    }
                })
                .startFire();
    }
}
