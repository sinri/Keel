package io.github.sinri.keel.test.lab.excel;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.poi.excel.KeelSheet;
import io.github.sinri.keel.poi.excel.KeelSheets;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.github.sinri.keel.tesuto.TestUnitResult;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class ReadHugeExcelTest extends KeelTest {
    private static final String file = "/Users/leqee/code/Keel/src/test/resources/excel/excel_0.xlsx";
    private KeelEventLogger logger;
//    private KeelSheets sheets;

    @Nonnull
    @Override
    protected KeelEventLogger logger() {
        return logger;
    }

    @Nonnull
    @Override
    protected Future<Void> starting() {
        this.logger = KeelOutputEventLogCenter.getInstance().createLogger(getClass().getSimpleName());
//        try {
//            this.excelStreamReader = new KeelStreamSheets(file);
//        } catch (IOException e) {
//            return Future.failedFuture(e);
//        }
        return Future.succeededFuture();
    }

    @Nonnull
    @Override
    protected Future<Void> ending(List<TestUnitResult> testUnitResults) {
//        try {
//            this.excelStreamReader.close();
//        } catch (IOException e) {
//            return Future.failedFuture(e);
//        }
        return Future.succeededFuture();
    }

    /**
     * Read 20w rows, SYNC.
     * Test Result: UNIT [test1] PASSED. Spent 10093 ms;
     */
    @TestUnit
    public Future<Void> test1() {
        var excelStreamReader = new KeelSheets(file);
        KeelSheet excelSheetReader = excelStreamReader.generateReaderForSheet(0);
        AtomicInteger x = new AtomicInteger(0);
        excelSheetReader.blockReadAllRows(row -> {
            String index = row.getCell(0).getStringCellValue();
            String spent = row.getCell(1).getStringCellValue();
//            this.logger().info("index: " + index + " spent: " + spent);
            x.incrementAndGet();
        });
        excelStreamReader.close();
        this.logger().info("FIN 1 " + x.get());

        return Future.succeededFuture();
    }

    /**
     * Read 20w rows, ASYNC.
     *
     * @return
     */
    @TestUnit
    public Future<Void> test2() {
        var excelStreamReader = new KeelSheets(file);
        KeelSheet excelSheetReader = excelStreamReader.generateReaderForSheet(0);
        AtomicInteger x = new AtomicInteger(0);
        return excelSheetReader.readAllRows(rows -> {
//                    String index = row.getCell(0).getStringCellValue();
//                    String spent = row.getCell(1).getStringCellValue();
//                    this.logger().info("index: " + index + " spent: " + spent);
                    x.addAndGet(rows.size());
                    return Future.succeededFuture();
                }, 1000)
                .andThen(ar -> {
                    excelStreamReader.close();
                })
                .compose(v -> {
                    this.logger().info("FIN 2 " + x.get());
                    return Future.succeededFuture();
                });
    }
}
