package io.github.sinri.keel.test.hazelcast.hourglass;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.maids.hourglass.KeelPureHourglass;
import io.vertx.core.DeploymentOptions;

import java.util.Date;

public class HourglassTest {
    public static void testPure() {
        KeelPureHourglass keelPureHourglass = new KeelPureHourglass(HourglassTest.class.getSimpleName());
        //keelPureHourglass.setLogger(Keel.outputLogger());
        keelPureHourglass.setInterval(60_000L).setHandler(now -> {
            String dateExpression = Keel.helpers().datetime().getDateExpression(new Date(now), "yyyy-MM-dd HH:mm");
            keelPureHourglass.getLogger().info("RUN " + dateExpression + " triggered : " + now);
        });
        Keel.getVertx().deployVerticle(keelPureHourglass, new DeploymentOptions().setWorker(true));
    }
}
