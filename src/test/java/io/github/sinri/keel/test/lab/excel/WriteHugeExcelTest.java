package io.github.sinri.keel.test.lab.excel;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.poi.excel.KeelSheet;
import io.github.sinri.keel.poi.excel.KeelSheets;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.github.sinri.keel.tesuto.TestUnitResult;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class WriteHugeExcelTest extends KeelTest {
    private static final String file = "/Users/leqee/code/Keel/src/test/resources/excel/excel_3.xlsx";


    @Nonnull
    @Override
    protected Future<Void> starting() {
        return Future.succeededFuture();
    }

    @Nonnull
    @Override
    protected Future<Void> ending(List<TestUnitResult> testUnitResults) {
        return Future.succeededFuture();
    }

    //@TestUnit
    public Future<Void> test1() {
        KeelSheets sheets = new KeelSheets();
        KeelSheet sheet = sheets.generateWriterForSheet("Needs");
        sheet.blockWriteAllRows(List.of(
                List.of("Name", "Need", "Note"),
                List.of("Tim", "Apple", "small"),
                List.of("Steve", "Pear", "round"),
                List.of("Wake", "Banana", "long")
        ), 10, 10);
        sheets.save(file);
        sheets.close();
        return Future.succeededFuture();
    }

    @TestUnit
    public Future<Void> testWriteNotStream() {
        KeelSheets sheets = new KeelSheets();
        KeelSheet sheet = sheets.generateWriterForSheet("Huge");

        return write20wRows(sheet)
                .compose(v -> {
                    sheets.save(file);
                    sheets.close();
                    return Future.succeededFuture();
                });
    }

    @TestUnit
    public Future<Void> testWriteStream() {
        KeelSheets sheets = new KeelSheets();
        sheets.useStreamWrite();
        KeelSheet sheet = sheets.generateWriterForSheet("Huge");

        return write20wRows(sheet)
                .compose(v -> {
                    sheets.save(file);
                    sheets.close();
                    return Future.succeededFuture();
                });
    }


    private Future<Void> write20wRows(KeelSheet sheet) {
        long startTime = System.currentTimeMillis();

        List<String> headerRow = new ArrayList<>();
        headerRow.add("INDEX");
        headerRow.add("SPENT");
        sheet.blockWriteAllRows(List.of(headerRow));

        return KeelAsyncKit.stepwiseCall(200, ii -> {
            List<List<String>> buffer = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                List<String> row = new ArrayList<>();
                row.add(String.valueOf(ii * 1000 + i));
                row.add(String.valueOf((System.currentTimeMillis() - startTime) / 1000.0));
                buffer.add(row);
            }
            sheet.blockWriteAllRows(buffer, ii * 1000, 0);
            return Future.succeededFuture();
        });
    }
}
