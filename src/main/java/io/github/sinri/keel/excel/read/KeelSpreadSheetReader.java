package io.github.sinri.keel.excel.read;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import io.github.sinri.keel.facade.Keel;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @since 3.0.8
 */
public class KeelSpreadSheetReader {

    public Future<SpreadSheetMatrix> readEntireSheet(Handler<ReaderOptions> readerOptionsBuilder) {
        ReaderOptions readerOptions = new ReaderOptions();
        readerOptionsBuilder.handle(readerOptions);
        return this.readEntireSheet(readerOptions);
    }

    public Future<SpreadSheetMatrix> readEntireSheet(ReaderOptions readerOptions) {
        return Keel.getVertx().executeBlocking(promise -> {
            try {
                var x = this.blockReadEntireSheet(readerOptions);
                promise.complete(x);
            } catch (Throwable e) {
                promise.fail(e);
            }
        });
    }

    private SpreadSheetMatrix blockReadEntireSheet(ReaderOptions readerOptions) throws Throwable {
        SpreadSheetMatrix matrix = new SpreadSheetMatrix();
        PageReadListener<LinkedHashMap<Integer, String>> listener = new PageReadListener<>(rows -> rows
                .forEach(row -> {
                    List<String> rowAsList = new ArrayList<>();
                    row.forEach((k, v) -> rowAsList.add(v));
                    matrix.addRow(rowAsList);
                }));

        ExcelReaderBuilder excelReaderBuilder = new ExcelReaderBuilder();
        if (readerOptions.getFileName() != null) {
            excelReaderBuilder.file(readerOptions.getFileName());
        } else {
            excelReaderBuilder.file(readerOptions.getFileInputStream());
        }

        excelReaderBuilder.registerReadListener(listener);
        try (ExcelReader excelReader = excelReaderBuilder.build()) {
            ReadSheet readSheet = EasyExcel.readSheet(readerOptions.getSheetNo(), readerOptions.getSheetName())
                    .headRowNumber(readerOptions.getHeadRowNumber())
                    .build();
            excelReader.read(readSheet);
            return matrix;
        }
    }
}
