package io.github.sinri.keel.excel.read;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.metadata.ReadSheet;
import io.github.sinri.keel.facade.Keel;
import io.vertx.core.Future;

import java.io.InputStream;

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
        return Keel.getVertx().executeBlocking(() -> blockReadEntireSheet(sheetReadOptions));
    }

    private SpreadSheetMatrix blockReadEntireSheet(SheetReadOptions sheetReadOptions) {
        SpreadSheetMatrixRowCollector spreadSheetMatrixRowCollector = new SpreadSheetMatrixRowCollector();
        excelReaderBuilder.registerReadListener(spreadSheetMatrixRowCollector);
        try (ExcelReader excelReader = excelReaderBuilder.build()) {
            ReadSheet readSheet = EasyExcel.readSheet(sheetReadOptions.getSheetNo(), sheetReadOptions.getSheetName())
                    .headRowNumber(sheetReadOptions.getHeadRowNumber())
                    .build();
            excelReader.read(readSheet);

            return spreadSheetMatrixRowCollector.getMatrix();
        }
    }

}
