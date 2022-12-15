package io.github.sinri.keel.test.hazelcast.watch;

import io.github.sinri.keel.core.KeelCronExpression;
import io.github.sinri.keel.lagecy.Keel;
import io.github.sinri.keel.maids.watchman.KeelCronWatchman;
import io.github.sinri.keel.maids.watchman.KeelPureWatchman;
import io.github.sinri.keel.maids.watchman.KeelWatchmanEventHandler;

import java.util.Date;

public class WatchTest {
    public static void testPure() {
        KeelPureWatchman.deploy("WatchPureTest", options -> options
                        .setInterval(60_000L)
                        .setHandler(now -> {
                            String dateExpression = Keel.helpers().datetime().getDateExpression(new Date(now), "yyyy-MM-dd HH:mm");
                            Keel.outputLogger("WatchPureTest").info("RUN " + dateExpression + " triggered : " + now);
                        })
                )
                .andThen(ar -> {
                    if (ar.failed()) {
                        Keel.outputLogger().exception(ar.cause());
                    } else {
                        Keel.outputLogger().info(ar.result());
                    }
                });
    }

    public static void testCron() {
        KeelCronWatchman.deploy("WatchCronTest", asyncMapName -> {
                    return KeelCronWatchman.addCronJobToAsyncMap(
                            asyncMapName,
                            new KeelCronExpression("* * * * *"),
                            CronEventHandler.class
                    );
                })
                .andThen(ar -> {
                    if (ar.failed()) {
                        Keel.outputLogger().exception(ar.cause());
                    } else {
                        Keel.outputLogger().info(ar.result());
                    }
                });
    }

    public static class CronEventHandler implements KeelWatchmanEventHandler {

        @Override
        public void handle(Long time) {
            Keel.outputLogger("CronEventHandler").info("GO ON " + time);
        }
    }
}
