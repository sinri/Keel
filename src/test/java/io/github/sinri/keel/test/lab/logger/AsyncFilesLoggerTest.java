package io.github.sinri.keel.test.lab.logger;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.adapter.AsyncFilesWriterAdapter;
import io.github.sinri.keel.logger.event.center.KeelAsyncEventLogCenter;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import java.util.Date;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class AsyncFilesLoggerTest {
    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions())
                .compose(v -> {
                    return test();
                })
                .eventually(() -> Keel.getVertx().close());
    }

    private static Future<Void> test() {
        KeelAsyncEventLogCenter logCenter = new KeelAsyncEventLogCenter(new AsyncFilesWriterAdapter("/Users/leqee/code/Keel/log"));
        KeelEventLogger t1 = logCenter.createLogger("t1.t2");
        return KeelAsyncKit.stepwiseCall(5, i -> {
            t1.info(event -> {
                event.message("now: " + new Date());
                event.context(c -> c.put("t", "1"));
            });
            return KeelAsyncKit.sleep(1000L);
        });
    }
}
