package io.github.sinri.keel.test.cache.alef;

import io.github.sinri.keel.cache.impl.KeelCacheAlef;
import io.github.sinri.keel.lagecy.Keel;
import io.github.sinri.keel.lagecy.core.logger.KeelLogger;
import io.github.sinri.keel.test.SharedTestBootstrap;

public class KeelCacheAlefTest {
    public static final KeelCacheAlef<String, Long> alef = new KeelCacheAlef<>();

    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v -> test1());
    }

    private static void test1() {
        KeelLogger logger = Keel.standaloneLogger("KeelCacheAlefTest-1");
        for (int i = 0; i < 5; i++) {
            Alef1 alef1 = new Alef1(logger);

            alef1.deployMe();
        }

        Keel.getVertx().setTimer(10_000L, timerID -> {
            Keel.getVertx().close();
        });
    }
}
