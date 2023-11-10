package io.github.sinri.keel.test.lab.excel;

import io.github.sinri.keel.excel.read.KeelSpreadSheetReader;
import io.github.sinri.keel.excel.read.SheetReadOptions;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.github.sinri.keel.tesuto.TestUnitResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import org.jetbrains.annotations.NotNull;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class ExcelHeaderTest extends KeelTest {

    @Override
    protected @NotNull KeelEventLogger logger() {
        return KeelOutputEventLogCenter.instantLogger();
    }

    @NotNull
    @Override
    protected Future<Void> starting() {
        return Future.succeededFuture();
    }

    @NotNull
    @Override
    protected Future<Void> ending(List<TestUnitResult> testUnitResults) {
        return Future.succeededFuture();
    }

    @TestUnit
    public Future<Void> test() {
        try {
            String sampleExcelFile = "/Users/leqee/code/Keel/src/test/resources/excel/excel_2.xlsx";
            InputStream inputStream = new FileInputStream(sampleExcelFile);
            return new KeelSpreadSheetReader(inputStream)
                    .readEntireSheet(SheetReadOptions.forIndex(0))
                    .compose(spreadSheetMatrix -> {
                        List<List<String>> headerList = spreadSheetMatrix.getHeaders();
                        logger().info("headerList:" + new JsonArray(headerList));
                        return Future.succeededFuture();
                    });
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }

    }
}
