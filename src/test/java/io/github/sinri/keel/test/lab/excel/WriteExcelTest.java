package io.github.sinri.keel.test.lab.excel;

import io.github.sinri.keel.excel.write.KeelSpreadSheetWriter;
import io.github.sinri.keel.excel.write.SheetWriteOptions;
import io.github.sinri.keel.facade.Keel;
import io.vertx.core.VertxOptions;

import java.util.List;

public class WriteExcelTest {
    public static void main(String[] args) {
        Keel.initializeVertxStandalone(new VertxOptions());

        String file = "/Users/leqee/code/Keel/src/test/resources/excel/excel_0.xlsx";
//        EasyExcel.write(file)
//                .head(List.of(List.of("id"),List.of("tile"),List.of("price")))
//                .sheet(0)
//                .doWrite(List.of(
//                        List.of("1","Apple","1.0"),
//                        List.of("2","Pear","2.0")
//                ));

        new KeelSpreadSheetWriter(file)
                .writeEntireSheet(SheetWriteOptions.forIndex(0), List.of(
                        List.of("dog", "1"),
                        List.of("cat", "2")
                ))
                .onComplete(ar -> {
                    if (ar.failed()) {
                        ar.cause().printStackTrace();
                    }
                    Keel.getVertx().close();
                });
    }
}
