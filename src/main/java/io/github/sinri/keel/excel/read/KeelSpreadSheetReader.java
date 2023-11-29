package io.github.sinri.keel.excel.read;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.builder.ExcelReaderBuilder;
import com.alibaba.excel.read.metadata.ReadSheet;
import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.vertx.core.Future;

import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

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

    @TechnicalPreview(since = "3.0.12")
    public Future<Void> readSheetStream(SheetReadOptions sheetReadOptions, Function<List<List<String>>, Future<Void>> processor) {
        return KeelAsyncKit.executeBlocking(promise -> {
            Future.succeededFuture()
                    .compose(v -> Future.succeededFuture(new SpreadSheetStreamRowCollector(processor, 10000)))
                    .compose(collector -> {
                        return collector.getStream()
                                .startIntravenous()
                                .compose(deploymentId -> {
                                    excelReaderBuilder.registerReadListener(collector);
                                    try (ExcelReader excelReader = excelReaderBuilder.build()) {
                                        ReadSheet readSheet = EasyExcel.readSheet(sheetReadOptions.getSheetNo(), sheetReadOptions.getSheetName())
                                                .headRowNumber(sheetReadOptions.getHeadRowNumber())
                                                .build();
                                        excelReader.read(readSheet);
                                        System.out.println("excelReader.read returned");

                                        return collector.getStream().shutdownIntravenous();
                                    }
                                });
                    })
                    .onComplete(ar -> {
                        if (ar.failed()) {
                            promise.fail(ar.cause());
                        } else {
                            promise.complete();
                        }
                    });
        });
    }
}
