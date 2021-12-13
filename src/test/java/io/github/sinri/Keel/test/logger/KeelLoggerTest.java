package io.github.sinri.Keel.test.logger;

import io.github.sinri.Keel.core.logger.KeelLogger;
import io.github.sinri.Keel.core.properties.KeelPropertiesReader;
import io.vertx.ext.unit.TestSuite;

public class KeelLoggerTest {

    public static void main(String[] args) {
        KeelPropertiesReader.registerReaderWithFile("KeelLogger", "KeelLogger.properties");

        TestSuite suite = TestSuite.create("KeelLoggerTestSuite");
        suite.test("stdout", context -> {
                    KeelLogger logger = new KeelLogger();
                    logger.debug("debug");
                    logger.info("info");
                    logger.notice("notice");
                    logger.warning("warning");
                    logger.error("error");
                    logger.fatal("fatal");
                })
                .test("stdout-with-aspect", testContext -> {
                    KeelLogger logger = new KeelLogger("aspect");
                    logger.debug("debug");
                    logger.info("info");
                    logger.notice("notice");
                    logger.warning("warning");
                    logger.error("error");
                    logger.fatal("fatal");
                })
//                .test("file_for_second", testContext -> {
//                    KeelLogger logger = new KeelLogger(new File("./log"),"rotate_by_second");
//                    logger.setLowestLevel(KeelLogLevel.NOTICE).setRotateDateTimeFormat("yyyyMMddHHmmss");
//                    for(int i=0;i<3;i++) {
//                        logger.debug("debug");
//                        logger.info("info");
//                        logger.notice("notice");
//                        logger.warning("warning");
//                        logger.error("error");
//                        logger.fatal("fatal");
//                        try {
//                            Thread.sleep(2000);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                })
                .test("with-properties", testContext -> {
                    KeelLogger logger = KeelLogger.use("x");
                    testContext.assertEquals(logger.getRotateDateTimeFormat(), "yyyyMMddHH");
                    logger.debug("debug");
                    logger.info("info");
                    logger.notice("notice");
                    logger.warning("warning");
                    logger.error("error");
                    logger.fatal("fatal");
                });
        suite.run();


    }
}
