package io.github.sinri.keel.test.core.logger;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.json.JsonObject;

public class LoggerTest1 {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        String a = Keel.getPropertiesReader().getProperty("LoggerTest1.a");
        Keel.outputLogger("main").info("a", new JsonObject().put("a", a));
        String b = Keel.getPropertiesReader().getProperty("LoggerTest1.b");
        Keel.outputLogger("main").info("b", new JsonObject().put("b", b));
        String c = Keel.getPropertiesReader().getProperty("LoggerTest1.c");
        Keel.outputLogger("main").info("c", new JsonObject().put("c", c));
    }
}
