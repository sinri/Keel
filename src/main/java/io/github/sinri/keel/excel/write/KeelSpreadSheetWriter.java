package io.github.sinri.keel.excel.write;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.vertx.core.Future;

import java.io.OutputStream;
import java.util.List;

/**
 * @since 3.0.8
 * @since 3.0.12 Remade, ref to <a href="https://easyexcel.opensource.alibaba.com/docs/current/quickstart/write#%E9%87%8D%E5%A4%8D%E5%A4%9A%E6%AC%A1%E5%86%99%E5%85%A5%E5%86%99%E5%88%B0%E5%8D%95%E4%B8%AA%E6%88%96%E8%80%85%E5%A4%9A%E4%B8%AAsheet">重复多次写入写到单个或者多个sheet</a>
 */
public class KeelSpreadSheetWriter {
    private final ExcelWriter excelWriter;

    public KeelSpreadSheetWriter(String filePath) {
        this.excelWriter = EasyExcel.write(filePath).build();
    }

    public KeelSpreadSheetWriter(OutputStream outputStream) {
        this.excelWriter = EasyExcel.write(outputStream).build();
    }

    public Future<Void> writeEntireSheet(SheetWriteOptions writeOptions, List<List<String>> dataRows) {
        return Keel.getVertx().executeBlocking(() -> {
            WriteSheet writeSheet = EasyExcel.writerSheet(writeOptions.getSheetNo(), writeOptions.getSheetName()).build();
            if (!writeOptions.getHeaders().isEmpty()) {
                excelWriter.write(writeOptions.getHeaders(), writeSheet);
            }
            excelWriter.write(dataRows, writeSheet);
            excelWriter.close();
            return null;
        });
    }

    @TechnicalPreview(since = "3.0.12")
    public Future<Void> writeSheetStream(SheetWriteOptions writeOptions, RowsGenerator rowsGenerator) {
        return KeelAsyncKit.executeBlocking(promise -> {
            Future.succeededFuture()
                    .compose(v -> {
                        WriteSheet writeSheet = EasyExcel.writerSheet(writeOptions.getSheetNo(), writeOptions.getSheetName()).build();
                        if (!writeOptions.getHeaders().isEmpty()) {
                            excelWriter.write(writeOptions.getHeaders(), writeSheet);
                        }
                        return KeelAsyncKit.repeatedlyCall(routineResult -> {
                            return rowsGenerator.generateRows()
                                    .compose(rows -> {
                                        if (rows == null) {
                                            routineResult.stop();
                                            return Future.succeededFuture();
                                        }
                                        if (!rows.isEmpty())
                                            excelWriter.write(rows, writeSheet);
                                        return Future.succeededFuture();
                                    });
                        });
                    })
                    .andThen(ar -> {
                        excelWriter.close();
                        if (ar.failed()) {
                            promise.fail(ar.cause());
                        } else {
                            promise.complete();
                        }
                    });
        });

    }

    public interface RowsGenerator {
        /**
         * @return Async result for rows, or null as finished.
         */
        Future<List<List<String>>> generateRows();
    }
}
