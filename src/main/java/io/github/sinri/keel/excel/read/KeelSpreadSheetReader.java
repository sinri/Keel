package io.github.sinri.keel.excel.read;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.listener.PageReadListener;
import com.alibaba.excel.read.metadata.ReadSheet;
import io.github.sinri.keel.facade.Keel;
import io.vertx.core.Future;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @since 3.0.8
 */
public class KeelSpreadSheetReader {
    private final ExcelReaderBuilder excelReaderBuilder = new ExcelReaderBuilder();

    public KeelSpreadSheetReader(String filePath) {
        excelReaderBuilder.file(filePath);
    }

    public KeelSpreadSheetReader(InputStream inputStream) {
        excelReaderBuilder.file(inputStream);
    }

    public Future<SpreadSheetMatrix> readEntireSheet(SheetReadOptions sheetReadOptions) {
        return Keel.getVertx().executeBlocking(promise -> {
            try {
                SpreadSheetMatrix matrix = this.blockReadEntireSheet(sheetReadOptions);
                promise.complete(matrix);
            } catch (Throwable e) {
                promise.fail(e);
            }
        });
    }

    private SpreadSheetMatrix blockReadEntireSheet(SheetReadOptions sheetReadOptions) throws Throwable {
        SpreadSheetMatrix matrix = new SpreadSheetMatrix();
        excelReaderBuilder.registerReadListener(new SpreadSheetMatrixRowCollector(matrix));
        try (ExcelReader excelReader = excelReaderBuilder.build()) {
            ReadSheet readSheet = EasyExcel.readSheet(sheetReadOptions.getSheetNo(), sheetReadOptions.getSheetName())
                    .headRowNumber(sheetReadOptions.getHeadRowNumber())
                    .build();
            excelReader.read(readSheet);
            return matrix;
        }
    }

    private static class SpreadSheetMatrixRowCollector extends PageReadListener<LinkedHashMap<Integer, String>> {

        public SpreadSheetMatrixRowCollector(SpreadSheetMatrix matrix) {
            super(rows -> rows
                    .forEach(row -> {
                        List<String> rowAsList = new ArrayList<>();
                        row.forEach((k, v) -> rowAsList.add(v));
                        matrix.addRow(rowAsList);
                    }));
        }
    }
}
