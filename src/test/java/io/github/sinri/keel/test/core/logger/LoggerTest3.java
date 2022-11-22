package io.github.sinri.keel.test.core.logger;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.core.logger.KeelLoggerOptions;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.json.JsonObject;

public class LoggerTest3 {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v -> {
            KeelLogger logger = Keel.outputLogger("json", options -> {
                options.setCompositionStyle(KeelLoggerOptions.CompositionStyle.ONE_JSON_OBJECT);
            });

            logger.info("WE ARE THE WAY!", new JsonObject().put("a", "b"));

            try {
                long a = 2;
                for (int i = 0; i < 2; i++) {
                    a = a * a;
                }
                long b = 16 - a;
                long c = a / b;
                logger.info("a=" + a + " b=" + b + " c=" + c);
            } catch (Exception e) {
                logger.exception("mmm", e);
            }

            try {
                try {
                    try {
                        throw new Exception("E1");
                    } catch (Exception e1) {
                        throw new Exception("E2", e1);
                    }
                } catch (Exception e2) {
                    throw new Exception("E2", e2);
                }
            } catch (Exception e3) {
                logger.exception("e3 is here", e3);
            }
        });


    }
}
