package io.github.sinri.keel.test.v1.cache;

import io.github.sinri.keel.cache.caffeine.CaffeineCacheKit;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;

public class CaffeineTest {
    static KeelLogger logger = new KeelLogger();
    static CaffeineCacheKit<String, String> x = new CaffeineCacheKit<>();

    public static void main(String[] args) {
//        try {
//            test1();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        test2();
//        test3();
    }

    private static void test1() throws InterruptedException {
        String key = "k1";
        logReadResult(key, null);

        String time1 = LocalDateTime.now().toString();
        x.save(key, time1, 4);
        logReadResult(key, time1);

        Thread.sleep(3000);
        logReadResult(key, time1);

        Thread.sleep(3000);
        logReadResult(key, null);

    }


    private static void test2() {
        logger.info("test for a lot of cache start");
        for (long i = 0; i < 10000000; i++) {
            x.save("TEST2-" + i, "VALUE FOR " + i, 20);
        }
        logger.info("test for a lot of cache end");
    }

    private static void test3() {
        for (int i = 1; i <= 6; i++) {
            x.save("3." + i, "" + i, 10);

            for (int j = 1; j <= 6; j++) {
                logReadResult("3." + j, j <= i ? ("" + j) : null);
            }

            if (i == 4) {
                x.remove("3.1");
                logger.warning("remove 3.1");
            }
            if (i == 5) {
                x.removeAll();
                logger.warning("remove all");
            }
        }
    }

    private static void logReadResult(String key, String expected) {
        String read = x.read(key);
        boolean same = false;
        if (expected == null) {
            same = (read == null);
        } else {
            same = expected.equals(read);
        }
        JsonObject object = new JsonObject().put("key", key).put("read", read).put("expected", expected);
        if (same) {
            logger.info("MATCHED", object);
        } else {
            logger.error("DIFFERED", object);
        }
    }
}
