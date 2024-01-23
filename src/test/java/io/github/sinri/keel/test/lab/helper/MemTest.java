package io.github.sinri.keel.test.lab.helper;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.ArrayList;
import java.util.List;

public class MemTest extends KeelTest {
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    @TestUnit
    public Future<Void> test() {
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
}
