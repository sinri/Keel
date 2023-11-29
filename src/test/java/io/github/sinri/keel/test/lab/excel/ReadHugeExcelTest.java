package io.github.sinri.keel.test.lab.excel;

import io.github.sinri.keel.excel.read.KeelSpreadSheetReader;
import io.github.sinri.keel.excel.read.SheetReadOptions;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.github.sinri.keel.tesuto.TestUnitResult;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ReadHugeExcelTest extends KeelTest {
    static String file = "/Users/leqee/code/Keel/src/test/resources/excel/excel_0.xlsx";

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

    @TestUnit
    public Future<Void> test1() {
        KeelEventLogger logger = KeelOutputEventLogCenter.getInstance().createLogger("test1");
        KeelSpreadSheetReader reader = new KeelSpreadSheetReader(file);
        AtomicInteger counter = new AtomicInteger(0);
        return reader.readSheetStream(SheetReadOptions.forIndex(0), rows -> {
                    counter.addAndGet(rows.size());
                    logger.info(log -> log.message("HANDLE ROWS")
                            .put("rows", rows)
                            .put("count", counter.get())
                    );
                    return KeelAsyncKit.sleep(1_00L);
                })
                .compose(v -> {
                    logger.info("ALL ROWS HANDLED: " + counter.get());
                    return Future.succeededFuture();
                });
    }
}
