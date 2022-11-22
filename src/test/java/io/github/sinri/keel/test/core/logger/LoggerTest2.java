package io.github.sinri.keel.test.core.logger;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.core.logger.KeelLoggerOptions;
import io.github.sinri.keel.test.SharedTestBootstrap;

public class LoggerTest2 {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v -> {
            KeelLoggerOptions keelLoggerOptions = new KeelLoggerOptions();

            logs(KeelLogger.createLogger(new KeelLoggerOptions().loadForAspect(LoggerTest2.class.getName())));
            logs(KeelLogger.silentLogger());
            keelLoggerOptions.setImplement("print");
            logs(KeelLogger.createLogger(keelLoggerOptions));
        });


    }

    private static void logs(KeelLogger logger) {
        logger.debug("msg");
        logger.info("msg");
        logger.notice("msg");
        logger.warning("msg");
        logger.error("msg");
        logger.fatal("msg");
        logger.exception("thrown", new Throwable("hello"));
        logger.reportCurrentRuntimeCodeLocation("here");


        try {
            new C1();
        } catch (Throwable t) {
            logger.exception("great", t);
        }
    }

    private static class C1 {
        public C1() {
            for (int i = 0; i < 3; i++) {
                try {
                    m1(i);
                } catch (ArithmeticException e) {
                    throw new RuntimeException("see!", e);
                }
            }
        }

        private int m1(int a) {
            int b = a - 1;
            return a / b;
        }
    }
}
