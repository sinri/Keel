package io.github.sinri.keel.test.servant;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureForRange;
import io.github.sinri.keel.core.controlflow.FutureSleep;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.servant.sisiodosi.KeelSisiodosi;
import io.github.sinri.keel.servant.sisiodosi.KeelSisiodosiWithTimer;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;

import java.util.Random;

public class KeelSisiodosiTest {
//    static KeelSisiodosi keelSisiodosi;

    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        KeelLogger logger = Keel.outputLogger();

        KeelSisiodosi.deployOneInstance(
                new KeelSisiodosiWithTimer.Options()
        ).onSuccess(
                sisiodosi -> {
                    sisiodosi.setLogger(logger);
                    FutureForRange.call(100, i -> {
                                int finalI = i;
                                sisiodosi.drop(() -> {
                                    logger.info("DRIP HANDLED " + finalI);
                                    return Future.succeededFuture();
                                });
                                int i1 = new Random().nextInt(300);
                                logger.info("DRIPPED " + finalI + " and then sleep " + (long) i1);
                                return FutureSleep.call(i1);
                            })
                            .onFailure(throwable -> {
                                logger.exception("RANGE ERROR", throwable);
                            })
                            .onComplete(v -> {
                                Keel.getVertx().setTimer(60_000L, timerID -> {
                                    Keel.getVertx().close();
                                });
                            });
                }
        );


    }
}
