package io.github.sinri.keel.test.lab.excel;

import io.github.sinri.keel.excel.write.KeelSpreadSheetWriter;
import io.github.sinri.keel.excel.write.SheetWriteOptions;
import io.github.sinri.keel.facade.Keel;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class WriteExcelTest {
    static String file = "/Users/leqee/code/Keel/src/test/resources/excel/excel_0.xlsx";

    public static void main(String[] args) {
        Keel.initializeVertxStandalone(new VertxOptions());


//        EasyExcel.write(file)
//                .head(List.of(List.of("id"),List.of("tile"),List.of("price")))
//                .sheet(0)
//                .doWrite(List.of(
//                        List.of("1","Apple","1.0"),
//                        List.of("2","Pear","2.0")
//                ));

        testHugeWrite()
                .onComplete(ar -> {
                    if (ar.failed()) {
                        ar.cause().printStackTrace();
                    }
                    Keel.getVertx().close();
                });


    }

    private static Future<Void> testEntireWrite() {
        return new KeelSpreadSheetWriter(file)
                .writeEntireSheet(SheetWriteOptions.forIndex(0), List.of(
                        List.of("dog", "1"),
                        List.of("cat", "2")
                ));
    }

    private static Future<Void> testHugeWrite() {
        return new KeelSpreadSheetWriter(file)
                .writeSheetStream(
                        SheetWriteOptions.forIndex(0)
                                .addHeaderRow(List.of("INDEX", "SPENT")),
                        new RowsGeneratorTest()
                );

//        try (ExcelWriter excelWriter = EasyExcel.write(file).build()) {
//            WriteSheet writeSheet = EasyExcel.writerSheet(0).build();
//            excelWriter.write(List.of(
//                    List.of("dog", "1"),
//                    List.of("cat", "2")
//            ),writeSheet);
//            excelWriter.write(List.of(
//                    List.of("duck", "3"),
//                    List.of("chicken", "4")
//            ),writeSheet);
//        }
//        return Future.succeededFuture();
    }

    private static class RowsGeneratorTest implements KeelSpreadSheetWriter.RowsGenerator {
        private final long startTime;
        private final AtomicInteger counter = new AtomicInteger(0);

        public RowsGeneratorTest() {
            startTime = System.currentTimeMillis();
        }

        @Override
        public Future<List<List<String>>> generateRows() {
            int start = counter.get();
            if (start >= 200000) return Future.succeededFuture(null);

            List<List<String>> rows = new ArrayList<>();
            for (int i = start; i < start + 1000; i++) {
                long now = System.currentTimeMillis();

                rows.add(List.of(
                        String.valueOf(i), String.valueOf((now - startTime) / 1000.0)
                ));

                counter.incrementAndGet();
            }
            return Future.succeededFuture(rows);
        }
    }
}
