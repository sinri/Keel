package io.github.sinri.keel.test.lab.csv;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.poi.csv.KeelCsvReader;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class ReadCsvTest extends KeelTest {
    @TestUnit
    public Future<Void> test() {
        String file = "/Users/leqee/code/Keel/src/test/resources/runtime/csv/write_test.csv";

        return KeelCsvReader.create(file, StandardCharsets.UTF_8)
                .compose(keelCsvReader -> {
                    AtomicInteger indexRef = new AtomicInteger(0);
                    return KeelAsyncKit.repeatedlyCall(routineResult -> {
                                return keelCsvReader.readRow()
                                        .compose(csvRow -> {
                                            if (csvRow == null) {
                                                logger().warning("CSV OVER");
                                                routineResult.stop();
                                            } else {
                                                JsonArray array = new JsonArray();
                                                for (int i = 0; i < csvRow.size(); i++) {
                                                    array.add(csvRow.getCell(i).getString());
                                                }
                                                logger().info(log -> log.message("ROW")
                                                        .put("i", indexRef.get())
                                                        .put("cell", array)
                                                );
                                                indexRef.incrementAndGet();
                                            }
                                            return Future.succeededFuture();
                                        });
                            })
                            .eventually(() -> {
                                return keelCsvReader.close();
                            })
                            .compose(v -> {
                                return Future.succeededFuture();
                            });
                });
    }
}
