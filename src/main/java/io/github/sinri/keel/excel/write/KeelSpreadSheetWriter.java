package io.github.sinri.keel.excel.write;

import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import io.github.sinri.keel.facade.Keel;
import io.vertx.core.Future;

import java.io.OutputStream;
import java.util.List;

/**
 * @since 3.0.8
 */
public class KeelSpreadSheetWriter {
    private final ExcelWriterBuilder excelWriterBuilder = new ExcelWriterBuilder();

    public KeelSpreadSheetWriter(String filePath) {
        excelWriterBuilder.file(filePath);
    }

    public KeelSpreadSheetWriter(OutputStream outputStream) {
        excelWriterBuilder.file(outputStream);
    }

    public Future<Void> writeEntireSheet(SheetWriteOptions writeOptions, List<List<String>> dataRows) {
        return Keel.getVertx().executeBlocking(promise -> {
            try {
                this.writeSheet(writeOptions, dataRows);
                promise.complete();
            } catch (Throwable e) {
                promise.fail(e);
            }
        });
    }

    private void writeSheet(SheetWriteOptions writeOptions, List<List<String>> dataRows) throws Throwable {
        excelWriterBuilder.sheet(writeOptions.getSheetNo(), writeOptions.getSheetName())
                .head(writeOptions.getHeaders())
                .doWrite(dataRows);
    }
}
