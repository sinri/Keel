package io.github.sinri.keel.test.hazelcast.hourglass;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.maids.hourglass.KeelPureHourglass;
import io.vertx.core.DeploymentOptions;

public class HourglassTest {
    public static void testPure() {
        KeelPureHourglass keelPureHourglass = new KeelPureHourglass(HourglassTest.class.getSimpleName());
        keelPureHourglass.setLogger(Keel.outputLogger());
        keelPureHourglass.setInterval(5000L).setHandler(now -> {
            keelPureHourglass.getLogger().info("RUN, NOW: " + now);
        });
        Keel.getVertx().deployVerticle(keelPureHourglass, new DeploymentOptions().setWorker(true));
    }
}
