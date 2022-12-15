package io.github.sinri.keel.test.v1.logger;

import io.github.sinri.keel.lagecy.Keel;
import io.github.sinri.keel.lagecy.core.logger.KeelLogger;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestSuite;



public class KeelLoggerTest {

    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions().setWorkerPoolSize(2));
        Keel.loadPropertiesFromFile("config.properties");

        TestSuite suite = TestSuite.create("KeelLoggerTestSuite");
        suite.test("stdout", context -> {
                    KeelLogger logger = Keel.outputLogger("aspect");
                    logger.debug("debug");
                    logger.info("info");
                    logger.notice("notice");
                    logger.warning("warning");
                    logger.error("error");
                    logger.fatal("fatal");
                })
                .test("stdout-with-aspect", testContext -> {
                    KeelLogger logger = Keel.outputLogger("aspect");

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
                    KeelLogger logger = Keel.outputLogger("x");
                    //testContext.assertEquals(logger.getRotateDateTimeFormat(), "yyyyMMddHH");
                    logger.debug("debug");
                    logger.info("info");
                    logger.notice("notice");
                    logger.warning("warning");
                    logger.error("error");
                    logger.fatal("fatal");
                })
                .test("check-properties", testContext -> {
                    KeelLogger logger = Keel.outputLogger("x");

                    JsonObject jsonObject = Keel.getPropertiesReader().toJsonObject();
                    logger.notice("properties to json", jsonObject);
                })
                .test("use-properties", testContext -> {
                    KeelLogger logger = Keel.standaloneLogger("x");

                    JsonObject jsonObject = Keel.getPropertiesReader().toJsonObject();
                    logger.notice("properties to json", jsonObject);
                });
        suite.run();


    }

    public static void test1() {

    }
}
