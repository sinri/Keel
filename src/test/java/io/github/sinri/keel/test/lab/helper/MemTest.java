package io.github.sinri.keel.test.lab.helper;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class MemTest extends KeelTest {
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    @TestUnit(skip = true)
    public Future<Void> test1() {
        List<MemoryBlock> list = new ArrayList<>();
        return KeelAsyncKit.stepwiseCall(100, i -> {
            list.add(new MemoryBlock());
            printMemoryUsage();
            return KeelAsyncKit.sleep(10L)
                    .compose(v -> {
                        return Future.succeededFuture();
                    });
        });
    }

    @TestUnit(skip = true)
    public Future<Void> test2() {
        double a = 1.3;
        double b = a * 2;
        double c = new Random().nextDouble();
        double x = 1.0 / ((b - a) * c - a * c);
        logger().info("result", new JsonObject()
                .put("x", x)
                .put("isFinite", Double.isFinite(x))
                .put("isInfinite", Double.isInfinite(x))
                .put("isNaN", Double.isNaN(x))
        );


        return Future.succeededFuture();
    }

    private void printMemoryUsage() {
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();
        logger().info(log -> log.message("mem")
                .put("heap", new JsonObject()
                        .put("init", heapMemoryUsage.getInit() / (1024 * 2024 * 1.0))
                        .put("used", heapMemoryUsage.getUsed() / (1024 * 2024 * 1.0))
                        .put("committed", heapMemoryUsage.getCommitted() / (1024 * 2024 * 1.0))
                        .put("max", heapMemoryUsage.getMax() / (1024 * 2024 * 1.0))
                )
                .put("non_heap", new JsonObject()
                        .put("init", nonHeapMemoryUsage.getInit() / (1024 * 2024 * 1.0))
                        .put("used", nonHeapMemoryUsage.getUsed() / (1024 * 2024 * 1.0))
                        .put("committed", nonHeapMemoryUsage.getCommitted() / (1024 * 2024 * 1.0))
                        .put("max", nonHeapMemoryUsage.getMax() / (1024 * 2024 * 1.0))
                )
        );
    }

    private static class MemoryBlock {
        private final byte[] data = new byte[100 * 1024 * 1024];

        public byte[] getData() {
            return data;
        }
    }

    @TestUnit
    public Future<Void> test3() {
        logger().info("point 1");
        AtomicInteger iRef = new AtomicInteger(2);
        Promise<Void> promise = Promise.promise();
        Keel.getVertx().setPeriodic(3000L, 2000L, timer -> {
            logger().info("point 2");
            if (iRef.decrementAndGet() <= 0) {
                if (Keel.getVertx().cancelTimer(timer)) {
                    promise.complete();
                } else {
                    promise.fail("???");
                }
            }
        });
        return promise.future();
    }
}
