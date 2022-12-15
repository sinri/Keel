package io.github.sinri.keel.test.cache.alef;

import io.github.sinri.keel.cache.impl.KeelCacheBet;
import io.github.sinri.keel.lagecy.Keel;
import io.github.sinri.keel.lagecy.core.logger.KeelLogger;
import io.github.sinri.keel.test.SharedTestBootstrap;

public class KeelCacheBetTest {
    public static KeelCacheBet<String, Long> bet = new KeelCacheBet<>();

    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v -> test1());
    }

    private static void test1() {
        KeelLogger logger = Keel.standaloneLogger("KeelCacheBetTest-1");
        for (int i = 0; i < 5; i++) {
            Bet1 bet1 = new Bet1(logger);

            bet1.deployMe();
        }

        Keel.getVertx().setTimer(10_000L, timerID -> {
            Keel.getVertx().close();
        });
    }
}
