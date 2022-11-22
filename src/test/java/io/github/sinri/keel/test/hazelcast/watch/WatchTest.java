package io.github.sinri.keel.test.hazelcast.watch;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.maids.watchman.KeelCronWatchman;
import io.github.sinri.keel.maids.watchman.KeelPureWatchman;
import io.github.sinri.keel.maids.watchman.KeelWatchmanEventHandler;
import io.github.sinri.keel.servant.sundial.KeelCronExpression;

import java.util.Date;
import java.util.List;

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
                    return KeelCronWatchman.updateCronTabToAsyncMap(
                            asyncMapName,
                            new KeelCronExpression("* * * * *"),
                            List.of(CronEventHandler.class)
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
