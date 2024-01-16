package io.github.sinri.keel.test.lab.csv;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.poi.csv.KeelCsvWriter;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;

public class WriteCsvTest extends KeelTest {
    @TestUnit
    public Future<Void> test1() {
        String file = "/Users/leqee/code/Keel/src/test/resources/runtime/csv/write_test.csv";

        List<List<String>> rows = new ArrayList<>();
        rows.add(List.of("Name", "Age", "Number"));
        rows.add(List.of("Asana", "18", "1.0"));
        rows.add(List.of("Bi'zu'mi", "-73", "10000000000000.0"));
        rows.add(List.of("Ca\"la\"pa", "10000000000003", "-2.212341242345235"));
        rows.add(List.of("Da\nnue", "-1788888883427777", "0.0"));

        return KeelCsvWriter.create(file)
                .compose(keelCsvWriter -> {
                    return KeelAsyncKit.iterativelyCall(rows, keelCsvWriter::writeRow)
                            .compose(v -> {
                                return keelCsvWriter.close();
                            });
                });
    }
}
