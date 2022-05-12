package io.github.sinri.keel.test.v1.logger;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;

public class KeelLoggerCategoryPrefixTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        Keel.logger("a").notice("a");
        Keel.logger("a/b").notice("a/b");
        Keel.logger("a").setCategoryPrefix("x").notice("a+x");
        Keel.logger("a/b").setCategoryPrefix("x").notice("a/b+x");
        Keel.logger("a/b").setCategoryPrefix("y").notice("a/b+y 1");
        Keel.logger("a/b").notice("a/b+y 2");
        Keel.logger("a/b").setCategoryPrefix("z").notice("a/b+z");

        Keel.getVertx().close();
    }
}
