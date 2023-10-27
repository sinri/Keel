package io.github.sinri.keel.excel.read;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import io.github.sinri.keel.facade.Keel;
import io.vertx.core.Future;
import io.vertx.core.WorkerExecutor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @since 3.0.8
 */
public class SpreadSheetReader {
    private final WorkerExecutor workerExecutor;

    public SpreadSheetReader(WorkerExecutor workerExecutor) {
        this.workerExecutor = workerExecutor;
    }

    public SpreadSheetReader() {
        this.workerExecutor = Keel.getVertx().createSharedWorkerExecutor(this.getClass().getName());
    }

    public Future<SpreadSheetMatrix> readAllRowsFromExcelSheet(
            @Nonnull String excelFileName,
            @Nullable Integer sheetNo,
            @Nullable String sheetName
    ) {
        return this.workerExecutor.executeBlocking(promise -> {
            SpreadSheetMatrix matrix = new SpreadSheetMatrix();
            try (ExcelReader excelReader = EasyExcel.read(
                            excelFileName,
                            null,
                            new PageReadListener<LinkedHashMap<Integer, String>>(rows -> {
                                rows.forEach(row -> {
                                    List<String> rowAsList = new ArrayList<>();
                                    row.forEach((k, v) -> rowAsList.add(v));
                                    matrix.addRow(rowAsList);
                                });
                            })
                    )
                    .build()
            ) {
                ReadSheet readSheet = EasyExcel.readSheet(sheetNo, sheetName).build();
                excelReader.read(readSheet);
                promise.complete(matrix);
            } catch (Throwable throwable) {
                promise.fail(throwable);
            }
        });
    }
}
