package io.github.sinri.keel.test.v1.logger;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;

public class KeelLoggerCategoryPrefixTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v -> {
            Keel.standaloneLogger("a").notice("a");
            Keel.standaloneLogger("a/b").notice("a/b");
            Keel.standaloneLogger("a").setCategoryPrefix("x").notice("a+x");
            Keel.standaloneLogger("a/b").setCategoryPrefix("x").notice("a/b+x");
            Keel.standaloneLogger("a/b").setCategoryPrefix("y").notice("a/b+y 1");
            Keel.standaloneLogger("a/b").notice("a/b+y 2");
            Keel.standaloneLogger("a/b").setCategoryPrefix("z").notice("a/b+z");

            Keel.getVertx().close();
        });


    }
}
