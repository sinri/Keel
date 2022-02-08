package io.github.sinri.keel.test.cache;

import io.github.sinri.keel.cache.caffeine.CaffeineCacheKitV1;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;

public class CaffeineTest {
    static KeelLogger logger = new KeelLogger();
    static CaffeineCacheKitV1<String> x = new CaffeineCacheKitV1<>();

    public static void main(String[] args) {
        try {
            test1();
            test2();
            test3();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static void test1() throws InterruptedException {
        String key = "k1";
        logReadResult(key, null);

        String time1 = LocalDateTime.now().toString();
        x.save(key, time1);
        logReadResult(key, time1);

        Thread.sleep(6000);
        logReadResult(key, time1);

        Thread.sleep(6000);
        logReadResult(key, null);

    }


    private static void test2() {
        String key = "k2";

        x.save(key, "1");
        logReadResult(key, "1");
        x.save(key, "2");
        logReadResult(key, "2");
        x.save(key, "3");
        logReadResult(key, "3");
        x.save(key, "4");
        logReadResult(key, "4");
        x.save(key, "5");
        logReadResult(key, "5");
    }

    private static void test3() {
        for (int i = 1; i <= 6; i++) {
            x.save("3." + i, "" + i);

            for (int j = 1; j <= 6; j++) {
                logReadResult("3." + j, j <= i ? ("" + j) : null);
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
