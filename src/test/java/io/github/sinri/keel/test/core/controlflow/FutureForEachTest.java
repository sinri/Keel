package io.github.sinri.keel.test.core.controlflow;

import io.github.sinri.keel.facade.async.FutureForEach;
import io.github.sinri.keel.facade.async.FutureSleep;
import io.github.sinri.keel.lagecy.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FutureForEachTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v -> {
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < 10000; i++) {
                list.add(i);
            }

            Random random = new Random();
            FutureForEach.call(
                            list,
                            i -> {
                                long x = random.nextInt(100);
                                Keel.outputLogger().info("FOR " + i + " spending " + x);
                                return FutureSleep.call(x);
                            }
                    )
                    .andThen(ar -> {
                        if (ar.failed()) {
                            Keel.outputLogger().exception(ar.cause());
                        } else {
                            Keel.outputLogger().info("DONE");
                        }
                        Keel.getVertx().close();
                    });
        });
    }
}
