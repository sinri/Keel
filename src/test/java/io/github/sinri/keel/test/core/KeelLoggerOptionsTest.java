package io.github.sinri.keel.test.core;

import io.github.sinri.keel.lagecy.Keel;
import io.github.sinri.keel.lagecy.core.logger.KeelLogLevel;
import io.github.sinri.keel.lagecy.core.logger.KeelLoggerOptions;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.json.JsonObject;

import java.util.Set;

public class KeelLoggerOptionsTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v -> {
            JsonObject jsonObject = Keel.getPropertiesReader().toJsonObject();
            System.out.println(jsonObject.encodePrettily());

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

                KeelLogLevel lowestLevel = new KeelLoggerOptions().loadForAspect("aspect").getLowestVisibleLogLevel();
                System.out.println(aspect + " -> " + lowestLevel);
            }

            Keel.getVertx().close();
        });


    }
}
