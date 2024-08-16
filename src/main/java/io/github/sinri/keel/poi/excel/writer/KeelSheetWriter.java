package io.github.sinri.keel.poi.excel.writer;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.poi.excel.KeelSheet;
import io.github.sinri.keel.poi.excel.reader.entity.KeelSheetMatrix;
import io.github.sinri.keel.poi.excel.reader.entity.KeelSheetTemplatedMatrix;
import io.vertx.core.Future;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since 3.2.16
 */
public class KeelSheetWriter extends KeelSheet {
    public KeelSheetWriter(@NotNull Sheet sheet) {
        super(sheet);
    }

    public void blockWriteAllRows(@NotNull List<List<String>> rowData, int sinceRowIndex, int sinceCellIndex) {
        for (int rowIndex = 0; rowIndex < rowData.size(); rowIndex++) {
            Row row = getSheet().getRow(sinceRowIndex + rowIndex);
            if (row == null) {
                row = getSheet().createRow(sinceRowIndex + rowIndex);
            }
            var rowDatum = rowData.get(rowIndex);
            writeToRow(row, rowDatum, sinceCellIndex);
        }
    }

    public void blockWriteAllRows(@NotNull List<List<String>> rowData) {
        blockWriteAllRows(rowData, 0, 0);
    }

    public void blockWriteMatrix(@NotNull KeelSheetMatrix matrix) {
        if (matrix.getHeaderRow().isEmpty()) {
            blockWriteAllRows(matrix.getRawRowList(), 0, 0);
        } else {
            blockWriteAllRows(List.of(matrix.getHeaderRow()), 0, 0);
            blockWriteAllRows(matrix.getRawRowList(), 1, 0);
        }
    }

    public Future<Void> writeMatrix(@NotNull KeelSheetMatrix matrix) {
        AtomicInteger rowIndexRef = new AtomicInteger(0);
        if (!matrix.getHeaderRow().isEmpty()) {
            blockWriteAllRows(List.of(matrix.getHeaderRow()), 0, 0);
            rowIndexRef.incrementAndGet();
        }

        return KeelAsyncKit.iterativelyBatchCall(matrix.getRawRowList().iterator(), rawRows -> {
            blockWriteAllRows(matrix.getRawRowList(), rowIndexRef.get(), 0);
            rowIndexRef.addAndGet(rawRows.size());
            return Future.succeededFuture();
        }, 1000);
    }

    public void blockWriteTemplatedMatrix(@NotNull KeelSheetTemplatedMatrix templatedMatrix) {
        AtomicInteger rowIndexRef = new AtomicInteger(0);
        blockWriteAllRows(List.of(templatedMatrix.getTemplate().getColumnNames()), 0, 0);
        rowIndexRef.incrementAndGet();
        templatedMatrix.getRows().forEach(templatedRow -> {
            blockWriteAllRows(List.of(templatedRow.getRawRow()), rowIndexRef.get(), 0);
        });
    }

    public Future<Void> writeTemplatedMatrix(@NotNull KeelSheetTemplatedMatrix templatedMatrix) {
        AtomicInteger rowIndexRef = new AtomicInteger(0);
        blockWriteAllRows(List.of(templatedMatrix.getTemplate().getColumnNames()), 0, 0);
        rowIndexRef.incrementAndGet();

        return KeelAsyncKit.iterativelyBatchCall(templatedMatrix.getRawRows().iterator(), rawRows -> {
            blockWriteAllRows(rawRows, rowIndexRef.get(), 0);
            rowIndexRef.addAndGet(rawRows.size());
            return Future.succeededFuture();
        }, 1000);
    }


    private void writeToRow(Row row, List<String> rowDatum, int sinceCellIndex) {
        for (int cellIndex = 0; cellIndex < rowDatum.size(); cellIndex++) {
            var cellDatum = rowDatum.get(cellIndex);

            Cell cell = row.getCell(cellIndex + sinceCellIndex);
            if (cell == null) {
                cell = row.createCell(cellIndex + sinceCellIndex);
            }

            cell.setCellValue(cellDatum);
        }
    }
}
