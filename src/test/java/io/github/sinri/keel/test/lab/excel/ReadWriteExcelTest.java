package io.github.sinri.keel.test.lab.excel;

import io.github.sinri.keel.poi.excel.KeelSheet;
import io.github.sinri.keel.poi.excel.KeelSheets;
import io.github.sinri.keel.poi.excel.entity.KeelSheetMatrixRow;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

public class ReadWriteExcelTest extends KeelTest {
    private static final String file = "/Users/leqee/code/Keel/src/test/resources/runtime/excel/excel_6.xlsx";


    @TestUnit(skip = true)
    public Future<Void> write() {
        KeelSheets keelSheets = KeelSheets.autoGenerateXLSX();
        KeelSheet keelSheet = keelSheets.generateWriterForSheet("First Sheet");
        keelSheet.blockWriteAllRows(List.of(
                List.of("name", "value"),
                List.of("A1", "1"),
                List.of("A2", "1.0"),
                List.of("A3", "1.1"),
                List.of("B1", "100000"),
                List.of("B2", "100000.0"),
                List.of("B3", "100000.1"),
                List.of("C1", "10000000000"),
                List.of("C2", "10000000000.0"),
                List.of("C3", "10000000000.1")
        ));
        keelSheets.save(file);
        keelSheets.close();
        return Future.succeededFuture();
    }

    @TestUnit(skip = false)
    public Future<Void> read() {
        KeelSheets keelSheets = KeelSheets.factory(file);
        KeelSheet keelSheet = keelSheets.generateReaderForSheet(0);
        return keelSheet.readAllRowsToMatrix(1, 2, null)
                .compose(matrix -> {
//                    matrix.getRawRowList().forEach(row -> {
//                        logger().info(log -> log.message("ROW")
//                                .put("row", row)
//                        );
//                    });
                    Iterator<RowModel> rowIterator = matrix.getRowIterator(RowModel.class);
                    while (rowIterator.hasNext()) {
                        RowModel rowModel = rowIterator.next();
                        String name = rowModel.readValue(0);
                        String raw = rowModel.readValue(1);
                        double d = rowModel.readValueToDouble(1);
                        Integer i = rowModel.readValueToInteger(1);
                        Long l = rowModel.readValueToLong(1);
                        BigDecimal b = rowModel.readValueToBigDecimal(1);
                        BigDecimal s = rowModel.readValueToBigDecimalStrippedTrailingZeros(1);
                        logger().info("Row [" + name + "]=" + raw + " d=" + d + " i=" + i + " l=" + l + " b=" + b.toPlainString() + " s=" + s.toPlainString());
                    }
                    keelSheets.close();
                    return Future.succeededFuture();
                });
    }

    public static class RowModel extends KeelSheetMatrixRow {

        public RowModel(List<String> rawRow) {
            super(rawRow);
        }
    }
}
