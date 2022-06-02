package io.github.sinri.keel.test.v1.servant;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.servant.KeelServantTimer;
import io.github.sinri.keel.servant.KeelServantTimerWorker;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import java.util.Calendar;

public class TimerTest1 {
    public static void main(String[] args) throws InterruptedException {
        Keel.initializeVertx(new VertxOptions().setWorkerPoolSize(2));
        Keel.loadPropertiesFromFile("test.sample.properties");

        KeelServantTimer keelServantTimer = new KeelServantTimer(Keel.getVertx());

        keelServantTimer.registerWorker("evert2min", new KeelServantTimerWorker("*/2 * * * *") {

            @Override
            protected KeelLogger getLogger() {
                return Keel.outputLogger("awaw");
            }

            @Override
            protected Future<Void> work(Calendar calendar) {
                System.out.println("evert2min triggered by " + calendar.getTime() + ", now START on " + Calendar.getInstance().getTime());

                return Keel.getVertx().executeBlocking(x -> {
                            try {
                                Thread.sleep(1000 * 60 * 3);
                                x.complete();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                                x.fail(e);
                            }
                        }).onComplete(ar -> {
                            System.out.println("evert2min triggered by " + calendar.getTime() + ", now END on " + Calendar.getInstance().getTime() + " ar:" + ar);
                        })
                        .compose(v -> {
                            return Future.succeededFuture();
                        });
            }
        });

        Thread.sleep(1000 * 60 * 5);
        keelServantTimer.stop();
        Thread.sleep(1000 * 60 * 5);
        Keel.getVertx().close();
    }
}
