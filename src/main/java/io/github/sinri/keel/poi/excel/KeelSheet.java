package io.github.sinri.keel.poi.excel;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.poi.excel.entity.KeelSheetMatrix;
import io.github.sinri.keel.poi.excel.entity.KeelSheetMatrixRowTemplate;
import io.github.sinri.keel.poi.excel.entity.KeelSheetTemplatedMatrix;
import io.vertx.core.Future;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

@TechnicalPreview(since = "3.0.13")
public class KeelSheet {
    private final Sheet sheet;

    public KeelSheet(Sheet sheet) {
        this.sheet = sheet;
    }

    /**
     * @return @return Raw Apache POI Sheet instance.
     */
    public Sheet getSheet() {
        return sheet;
    }

    public String getName() {
        return sheet.getSheetName();
    }

    public Row readRow(int i) {
        return getSheet().getRow(i);
    }

    public Iterator<Row> getRowIterator() {
        return getSheet().rowIterator();
    }

    /**
     * @since 3.0.14 add nullable to cell, and nonnull to return.
     */
    @Nonnull
    private static String dumpCellToString(@Nullable Cell cell) {
        if (cell == null) return "";
        CellType cellType = cell.getCellType();
        String s;
        if (cellType == CellType.NUMERIC) {
            double numericCellValue = cell.getNumericCellValue();
            s = String.valueOf(numericCellValue);
        } else {
            s = cell.getStringCellValue();
        }
        return Objects.requireNonNull(s);
    }

    private static List<String> dumpRowToRawRow(@Nonnull Row row, int maxColumns) {
        List<String> rowDatum = new ArrayList<>();

        for (int i = 0; i < maxColumns; i++) {
            @Nullable Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            String s = dumpCellToString(cell);
            rowDatum.add(s);
        }

        return rowDatum;
    }

    public final void blockReadAllRows(@Nonnull Consumer<Row> rowConsumer) {
        Iterator<Row> it = getRowIterator();

        while (it.hasNext()) {
            Row row = it.next();
            rowConsumer.accept(row);
        }
    }


    public final KeelSheetMatrix blockReadAllRowsToMatrix(int headerRowIndex, int maxColumns) {
        KeelSheetMatrix keelSheetMatrix = new KeelSheetMatrix();
        AtomicInteger rowIndex = new AtomicInteger(0);
        blockReadAllRows(row -> {
            int currentRowIndex = rowIndex.get();
            if (headerRowIndex == currentRowIndex) {
                keelSheetMatrix.setHeaderRow(dumpRowToRawRow(row, maxColumns));
            } else if (headerRowIndex < currentRowIndex) {
                keelSheetMatrix.addRow(dumpRowToRawRow(row, maxColumns));
            }

            rowIndex.incrementAndGet();
        });

        return keelSheetMatrix;
    }

    public final KeelSheetTemplatedMatrix blockReadAllRowsToTemplatedMatrix(int headerRowIndex, int maxColumns) {
        AtomicInteger rowIndex = new AtomicInteger(0);
        AtomicReference<KeelSheetTemplatedMatrix> templatedMatrixRef = new AtomicReference<>();
        blockReadAllRows(row -> {
            int currentRowIndex = rowIndex.get();
            if (currentRowIndex == headerRowIndex) {
                var rowDatum = dumpRowToRawRow(row, maxColumns);
                KeelSheetMatrixRowTemplate rowTemplate = KeelSheetMatrixRowTemplate.create(rowDatum);
                KeelSheetTemplatedMatrix templatedMatrix = KeelSheetTemplatedMatrix.create(rowTemplate);
                templatedMatrixRef.set(templatedMatrix);
            } else if (currentRowIndex > headerRowIndex) {
                var rowDatum = dumpRowToRawRow(row, maxColumns);
                templatedMatrixRef.get().addRawRow(rowDatum);
            }
            rowIndex.incrementAndGet();
        });
        return templatedMatrixRef.get();
    }

    /**
     * Consider calling this method in worker context.
     * Process row by row is not effective enough.
     */
    public final Future<Void> readAllRows(@Nonnull Function<Row, Future<Void>> rowFunc) {
        return KeelAsyncKit.iterativelyCall(getRowIterator(), rowFunc);
    }

    /**
     * Consider calling this method in worker context.
     */
    public final Future<Void> readAllRows(@Nonnull Function<List<Row>, Future<Void>> rowsFunc, int batchSize) {
        return KeelAsyncKit.iterativelyBatchCall(getRowIterator(), rowsFunc, batchSize);
    }

    public final Future<KeelSheetMatrix> readAllRowsToMatrix(int headerRowIndex, int maxColumns) {
        KeelSheetMatrix keelSheetMatrix = new KeelSheetMatrix();
        AtomicInteger rowIndex = new AtomicInteger(0);

        return readAllRows(rows -> {
            rows.forEach(row -> {
                int currentRowIndex = rowIndex.get();
                if (headerRowIndex == currentRowIndex) {
                    keelSheetMatrix.setHeaderRow(dumpRowToRawRow(row, maxColumns));
                } else if (headerRowIndex < currentRowIndex) {
                    keelSheetMatrix.addRow(dumpRowToRawRow(row, maxColumns));
                }
                rowIndex.incrementAndGet();
            });
            return Future.succeededFuture();
        }, 1000)
                .compose(v -> {
                    return Future.succeededFuture(keelSheetMatrix);
                });
    }

    public final Future<KeelSheetTemplatedMatrix> readAllRowsToTemplatedMatrix(int headerRowIndex, int maxColumns) {
        AtomicInteger rowIndex = new AtomicInteger(0);
        AtomicReference<KeelSheetTemplatedMatrix> templatedMatrixRef = new AtomicReference<>();

        return readAllRows(rows -> {
            rows.forEach(row -> {
                int currentRowIndex = rowIndex.get();
                if (currentRowIndex == headerRowIndex) {
                    var rowDatum = dumpRowToRawRow(row, maxColumns);
                    KeelSheetMatrixRowTemplate rowTemplate = KeelSheetMatrixRowTemplate.create(rowDatum);
                    KeelSheetTemplatedMatrix templatedMatrix = KeelSheetTemplatedMatrix.create(rowTemplate);
                    templatedMatrixRef.set(templatedMatrix);
                } else if (currentRowIndex > headerRowIndex) {
                    var rowDatum = dumpRowToRawRow(row, maxColumns);
                    templatedMatrixRef.get().addRawRow(rowDatum);
                }
                rowIndex.incrementAndGet();
            });
            return Future.succeededFuture();
        }, 1000)
                .compose(v -> {
                    return Future.succeededFuture(templatedMatrixRef.get());
                });
    }


    public void blockWriteAllRows(@Nonnull List<List<String>> rowData, int sinceRowIndex, int sinceCellIndex) {
        for (int rowIndex = 0; rowIndex < rowData.size(); rowIndex++) {
            Row row = sheet.getRow(sinceRowIndex + rowIndex);
            if (row == null) {
                row = sheet.createRow(sinceRowIndex + rowIndex);
            }
            var rowDatum = rowData.get(rowIndex);
            writeToRow(row, rowDatum, sinceCellIndex);
        }
    }

    public void blockWriteAllRows(@Nonnull List<List<String>> rowData) {
        blockWriteAllRows(rowData, 0, 0);
    }

    public void blockWriteMatrix(@Nonnull KeelSheetMatrix matrix) {
        if (matrix.getHeaderRow().isEmpty()) {
            blockWriteAllRows(matrix.getRawRowList(), 0, 0);
        } else {
            blockWriteAllRows(List.of(matrix.getHeaderRow()), 0, 0);
            blockWriteAllRows(matrix.getRawRowList(), 1, 0);
        }
    }

    public Future<Void> writeMatrix(@Nonnull KeelSheetMatrix matrix) {
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

    public void blockWriteTemplatedMatrix(@Nonnull KeelSheetTemplatedMatrix templatedMatrix) {
        AtomicInteger rowIndexRef = new AtomicInteger(0);
        blockWriteAllRows(List.of(templatedMatrix.getTemplate().getColumnNames()), 0, 0);
        rowIndexRef.incrementAndGet();
        templatedMatrix.getRows().forEach(templatedRow -> {
            blockWriteAllRows(List.of(templatedRow.getRawRow()), rowIndexRef.get(), 0);
        });
    }

    public Future<Void> writeTemplatedMatrix(@Nonnull KeelSheetTemplatedMatrix templatedMatrix) {
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
