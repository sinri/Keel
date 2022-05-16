package io.github.sinri.keel.test.core;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogLevel;
import io.github.sinri.keel.core.logger.KeelLoggerOptions;
import io.github.sinri.keel.test.SharedTestBootstrap;

import java.util.Set;

public class KeelLoggerOptionsTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        Set<String> aspects = Set.of(
                "y",
                "x",
                "x/a",
                "x/b",
                "x/a/a",
                "x/a/b",
                "x/a/b/c"
        );

        for (var aspect : aspects) {
            KeelLogLevel lowestLevel = KeelLoggerOptions.generateOptionsForAspectWithPropertiesReader(aspect).getLowestLevel();
            System.out.println(aspect + " -> " + lowestLevel);
        }

        Keel.getVertx().close();

    }
}
