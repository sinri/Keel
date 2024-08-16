package io.github.sinri.keel.poi.excel.reader;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.poi.excel.KeelSheet;
import io.github.sinri.keel.poi.excel.reader.entity.*;
import io.github.sinri.keel.poi.excel.reader.options.ColumnReadOptions;
import io.github.sinri.keel.poi.excel.reader.options.SheetReadOptions;
import io.vertx.core.Future;
import org.apache.poi.ss.usermodel.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @since 3.2.16
 */
public class KeelSheetReader extends KeelSheet {
    private final SheetReadOptions readOptions;

    /**
     * @since 3.2.16
     */
    public KeelSheetReader(@NotNull Sheet sheet, @NotNull SheetReadOptions readOptions) {
        super(sheet);
        this.readOptions = readOptions;
    }

    /**
     * @param row the POI row containing cells.
     * @return The number of cells from index zero to the last non-zero cell. If no cells, return 0.
     * @since 3.0.17 support auto detect column count
     */
    public static int autoDetectNonBlankColumnCountInOneRow(Row row) {
        short firstCellNum = row.getFirstCellNum();
        if (firstCellNum < 0) {
            return 0;
        }
        int i;
        for (i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell == null) {
                break;
            }
            if (cell.getCellType() != CellType.NUMERIC) {
                String stringCellValue = cell.getStringCellValue();
                if (stringCellValue == null || stringCellValue.isBlank()) break;
            }
        }
        return i;
    }

    /**
     * DUMP following those rules:
     * (1) NULL CELL to be "";
     * (2) NUMERIC CELL to be a double;
     * (3) FORMULA CELL to be a string (not set formula evaluator) or the computed value.
     * (4) ELSE to be the string expression.
     *
     * @since 3.0.14 add nullable to cell, and nonnull to return.
     * @since 3.1.3 return computed value for formula cells.
     * @since 3.1.4 add optional formulaEvaluator and becomes static again
     */
    @NotNull
    private static String dumpCellToString(
            @Nullable Cell cell,
            @NotNull SheetReadOptions readOptions,
            @Nullable ColumnReadOptions columnReadOptions
    ) {
        if (cell == null) return "";

        if (columnReadOptions == null) {
            columnReadOptions = readOptions.getDefaultColumnReadOptions();
        }

        CellType cellType = cell.getCellType();
        String s;
        if (cellType == CellType.NUMERIC) {
            double numericCellValue = cell.getNumericCellValue();

            if (columnReadOptions.isFormatDateTime() && DateUtil.isCellDateFormatted(cell)) {
                // 按需处理日期为指定格式的文本
                Date date = cell.getDateCellValue();
                s = columnReadOptions.getDateTimeFormat().format(date);
            } else {
                s = String.valueOf(numericCellValue);
            }
        } else if (cellType == CellType.FORMULA) {
            if (readOptions.getFormulaEvaluatorBox().isValueAlreadySet()) {
                CellType formulaResultType;

                @Nullable
                FormulaEvaluator formulaEvaluator = readOptions.getFormulaEvaluatorBox().getValue();

                if (formulaEvaluator == null) {
                    formulaResultType = cell.getCachedFormulaResultType();
                } else {
                    formulaResultType = formulaEvaluator.evaluateFormulaCell(cell);
                }
                switch (formulaResultType) {
                    case BOOLEAN:
                        s = String.valueOf(cell.getBooleanCellValue());
                        break;
                    case NUMERIC:
                        s = String.valueOf(cell.getNumericCellValue());
                        break;
                    case STRING:
                        s = String.valueOf(cell.getStringCellValue());
                        break;
                    case ERROR:
                        s = String.valueOf(cell.getErrorCellValue());
                        break;
                    default:
                        throw new RuntimeException("FormulaResultType unknown");
                }
            } else {
                return cell.getStringCellValue();
            }
        } else {
            s = cell.getStringCellValue();
        }
        return Objects.requireNonNull(s);
    }

    /**
     * @param sheetRowFilter added since 3.0.20
     * @since 3.0.20 add SheetRowFilter, and may return null if the row should be thrown.
     */
    private static @Nullable List<String> dumpRowToRawRow(
            @NotNull Row row,
            int maxColumns,
            @Nullable SheetRowFilter sheetRowFilter,
            @NotNull SheetReadOptions readOptions
    ) {
        List<String> rowDatum = new ArrayList<>();

        for (int i = 0; i < maxColumns; i++) {
            @Nullable Cell cell = row.getCell(i, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            ColumnReadOptions columnReadOptions = readOptions.getColumnReadOptions(i);
            String s = dumpCellToString(cell, readOptions, columnReadOptions);
            rowDatum.add(s);
        }

        // since 3.0.20
        if (sheetRowFilter != null) {
            if (sheetRowFilter.shouldThrowThisRawRow(rowDatum)) {
                return null;
            }
        }

        return rowDatum;
    }


    public Row readRow(int i) {
        return getSheet().getRow(i);
    }

    public Iterator<Row> getRowIterator() {
        return getSheet().rowIterator();
    }

    /**
     * @since 3.1.0
     */
    public List<String> readRawRow(int i, int maxColumns, @Nullable SheetRowFilter sheetRowFilter) {
        var row = readRow(i);
        return dumpRowToRawRow(row, maxColumns, sheetRowFilter, this.readOptions);
    }

    /**
     * @since 3.1.0
     */
    public Iterator<List<String>> getRawRowIterator(int maxColumns, @Nullable SheetRowFilter sheetRowFilter) {
        Iterator<Row> rowIterator = getRowIterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return rowIterator.hasNext();
            }

            @Override
            public List<String> next() {
                Row row = rowIterator.next();
                return dumpRowToRawRow(row, maxColumns, sheetRowFilter, readOptions);
            }
        };
    }

    public final void blockReadAllRows(@NotNull Consumer<Row> rowConsumer) {
        Iterator<Row> it = getRowIterator();

        while (it.hasNext()) {
            Row row = it.next();
            rowConsumer.accept(row);
        }
    }

    /**
     * @return A matrix read with rules: (1) first row as header, (2) auto-detect columns, (3) throw empty rows.
     * @since 3.0.20
     */
    public final KeelSheetMatrix blockReadAllRowsToMatrix() {
        return blockReadAllRowsToMatrix(0, 0, SheetRowFilter.toThrowEmptyRows());
    }

    /**
     * Fetch the matrix, the rows before header row would be thrown!
     *
     * @param headerRowIndex 0 for first row, etc.
     * @param maxColumns     For predictable, one or more columns; if auto-detection is needed, zero or less.
     * @since 3.0.17 support auto detect column count
     */
    public final KeelSheetMatrix blockReadAllRowsToMatrix(int headerRowIndex, int maxColumns, @Nullable SheetRowFilter sheetRowFilter) {
        if (headerRowIndex < 0) throw new IllegalArgumentException("headerRowIndex less than zero");

        KeelSheetMatrix keelSheetMatrix = new KeelSheetMatrix();
        AtomicInteger rowIndex = new AtomicInteger(0);

        AtomicInteger checkColumnsRef = new AtomicInteger();
        if (maxColumns > 0) {
            checkColumnsRef.set(maxColumns);
        }

        blockReadAllRows(row -> {
            int currentRowIndex = rowIndex.get();
            if (headerRowIndex == currentRowIndex) {
                if (checkColumnsRef.get() == 0) {
                    checkColumnsRef.set(autoDetectNonBlankColumnCountInOneRow(row));
                }
                List<String> headerRow = dumpRowToRawRow(row, checkColumnsRef.get(), sheetRowFilter, readOptions);
                if (headerRow == null) {
                    throw new NullPointerException("Header Row is not valid");
                }
                keelSheetMatrix.setHeaderRow(headerRow);
            } else if (headerRowIndex < currentRowIndex) {
                var x = dumpRowToRawRow(row, checkColumnsRef.get(), sheetRowFilter, readOptions);
                if (x != null) {
                    keelSheetMatrix.addRow(x);
                }
            }

            rowIndex.incrementAndGet();
        });

        return keelSheetMatrix;
    }

    /**
     * @return A matrix read with rules: (1) first row as header, (2) auto-detect columns, (3) throw empty rows.
     * @since 3.0.20
     */
    public final KeelSheetTemplatedMatrix blockReadAllRowsToTemplatedMatrix() {
        return blockReadAllRowsToTemplatedMatrix(0, 0, SheetRowFilter.toThrowEmptyRows());
    }

    /**
     * Fetch the templated matrix, the rows before header row would be thrown!
     *
     * @param headerRowIndex 0 for first row, etc.
     * @param maxColumns     For predictable, one or more columns; if auto-detection is needed, zero or less.
     * @since 3.0.17 support auto detect column count
     */
    public final KeelSheetTemplatedMatrix blockReadAllRowsToTemplatedMatrix(int headerRowIndex, int maxColumns, @Nullable SheetRowFilter sheetRowFilter) {
        if (headerRowIndex < 0) throw new IllegalArgumentException("headerRowIndex less than zero");

        AtomicInteger checkColumnsRef = new AtomicInteger();
        if (maxColumns > 0) {
            checkColumnsRef.set(maxColumns);
        }

        AtomicInteger rowIndex = new AtomicInteger(0);
        AtomicReference<KeelSheetTemplatedMatrix> templatedMatrixRef = new AtomicReference<>();


        blockReadAllRows(row -> {
            int currentRowIndex = rowIndex.get();
            if (currentRowIndex == headerRowIndex) {
                if (checkColumnsRef.get() == 0) {
                    checkColumnsRef.set(autoDetectNonBlankColumnCountInOneRow(row));
                }

                var rowDatum = dumpRowToRawRow(row, checkColumnsRef.get(), sheetRowFilter, readOptions);
                if (rowDatum == null) throw new NullPointerException("Header Row is not valid");
                KeelSheetMatrixRowTemplate rowTemplate = KeelSheetMatrixRowTemplate.create(rowDatum);
                KeelSheetTemplatedMatrix templatedMatrix = KeelSheetTemplatedMatrix.create(rowTemplate);
                templatedMatrixRef.set(templatedMatrix);
            } else if (currentRowIndex > headerRowIndex) {
                var rowDatum = dumpRowToRawRow(row, checkColumnsRef.get(), sheetRowFilter, readOptions);
                if (rowDatum != null) {
                    templatedMatrixRef.get().addRawRow(rowDatum);
                }
            }
            rowIndex.incrementAndGet();
        });
        return templatedMatrixRef.get();
    }

    /**
     * Consider calling this method in worker context.
     * Process row by row is not effective enough.
     */
    public final Future<Void> readAllRows(@NotNull Function<Row, Future<Void>> rowFunc) {
        return KeelAsyncKit.iterativelyCall(getRowIterator(), rowFunc);
    }

    /**
     * Consider calling this method in worker context.
     */
    public final Future<Void> readAllRows(@NotNull Function<List<Row>, Future<Void>> rowsFunc, int batchSize) {
        return KeelAsyncKit.iterativelyBatchCall(getRowIterator(), rowsFunc, batchSize);
    }

    /**
     * @return A future for matrix read with rules: (1) first row as header, (2) auto-detect columns, (3) throw empty rows.
     * @since 3.0.20
     */
    public final Future<KeelSheetMatrix> readAllRowsToMatrix() {
        return readAllRowsToMatrix(0, 0, SheetRowFilter.toThrowEmptyRows());
    }

    /**
     * Fetch the  matrix, the rows before header row would be thrown!
     *
     * @param headerRowIndex 0 for first row, etc.
     * @param maxColumns     For predictable, one or more columns; if auto-detection is needed, zero or less.
     * @since 3.0.17 support auto detect column count
     */
    public final Future<KeelSheetMatrix> readAllRowsToMatrix(int headerRowIndex, int maxColumns, @Nullable SheetRowFilter sheetRowFilter) {
        if (headerRowIndex < 0) throw new IllegalArgumentException("headerRowIndex less than zero");

        AtomicInteger checkColumnsRef = new AtomicInteger();
        if (maxColumns > 0) {
            checkColumnsRef.set(maxColumns);
        }

        KeelSheetMatrix keelSheetMatrix = new KeelSheetMatrix();
        AtomicInteger rowIndex = new AtomicInteger(0);

        return readAllRows(rows -> {
            rows.forEach(row -> {
                int currentRowIndex = rowIndex.get();
                if (headerRowIndex == currentRowIndex) {
                    if (checkColumnsRef.get() == 0) {
                        checkColumnsRef.set(autoDetectNonBlankColumnCountInOneRow(row));
                    }
                    var headerRow = dumpRowToRawRow(row, checkColumnsRef.get(), sheetRowFilter, readOptions);
                    if (headerRow == null) {
                        throw new NullPointerException("Header Row is not valid");
                    }
                    keelSheetMatrix.setHeaderRow(headerRow);
                } else if (headerRowIndex < currentRowIndex) {
                    List<String> rawRow = dumpRowToRawRow(row, checkColumnsRef.get(), sheetRowFilter, readOptions);
                    if (rawRow != null) {
                        keelSheetMatrix.addRow(rawRow);
                    }
                }
                rowIndex.incrementAndGet();
            });
            return Future.succeededFuture();
        }, 1000)
                .compose(v -> {
                    return Future.succeededFuture(keelSheetMatrix);
                });
    }

    /**
     * @return A future for matrix read with rules: (1) first row as header, (2) auto-detect columns, (3) throw empty rows.
     * @since 3.0.20
     */
    public final Future<KeelSheetTemplatedMatrix> readAllRowsToTemplatedMatrix() {
        return readAllRowsToTemplatedMatrix(0, 0, SheetRowFilter.toThrowEmptyRows());
    }

    /**
     * Fetch the templated matrix, the rows before header row would be thrown!
     *
     * @param headerRowIndex 0 for first row, etc.
     * @param maxColumns     For predictable, one or more columns; if auto-detection is needed, zero or less.
     * @since 3.0.17 support auto detect column count
     */
    public final Future<KeelSheetTemplatedMatrix> readAllRowsToTemplatedMatrix(int headerRowIndex, int maxColumns, @Nullable SheetRowFilter sheetRowFilter) {
        if (headerRowIndex < 0) throw new IllegalArgumentException("headerRowIndex less than zero");

        AtomicInteger checkColumnsRef = new AtomicInteger();
        if (maxColumns > 0) {
            checkColumnsRef.set(maxColumns);
        }

        AtomicInteger rowIndex = new AtomicInteger(0);
        AtomicReference<KeelSheetTemplatedMatrix> templatedMatrixRef = new AtomicReference<>();

        return readAllRows(rows -> {
            rows.forEach(row -> {
                int currentRowIndex = rowIndex.get();
                if (currentRowIndex == headerRowIndex) {
                    if (checkColumnsRef.get() == 0) {
                        checkColumnsRef.set(autoDetectNonBlankColumnCountInOneRow(row));
                    }

                    var rowDatum = dumpRowToRawRow(row, checkColumnsRef.get(), sheetRowFilter, readOptions);
                    if (rowDatum == null) {
                        throw new NullPointerException("Header Row is not valid");
                    }
                    KeelSheetMatrixRowTemplate rowTemplate = KeelSheetMatrixRowTemplate.create(rowDatum);
                    KeelSheetTemplatedMatrix templatedMatrix = KeelSheetTemplatedMatrix.create(rowTemplate);
                    templatedMatrixRef.set(templatedMatrix);
                } else if (currentRowIndex > headerRowIndex) {
                    var rowDatum = dumpRowToRawRow(row, checkColumnsRef.get(), sheetRowFilter, readOptions);
                    if (rowDatum != null) {
                        templatedMatrixRef.get().addRawRow(rowDatum);
                    }
                }
                rowIndex.incrementAndGet();
            });
            return Future.succeededFuture();
        }, 1000)
                .compose(v -> {
                    return Future.succeededFuture(templatedMatrixRef.get());
                });
    }

    /**
     * @since 3.2.11
     */
    @TechnicalPreview(since = "3.2.11")
    public Iterator<KeelSheetMatrixRow> getMatrixRowIterator(int maxColumns, @Nullable SheetRowFilter sheetRowFilter) {
        Iterator<List<String>> rawRowIterator = this.getRawRowIterator(maxColumns, sheetRowFilter);
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return rawRowIterator.hasNext();
            }

            @Override
            public KeelSheetMatrixRow next() {
                return new KeelSheetMatrixRow(rawRowIterator.next());
            }
        };

    }

    /**
     * @since 3.2.11
     */
    @TechnicalPreview(since = "3.2.11")
    public Iterator<KeelSheetMatrixTemplatedRow> getTemplatedMatrixRowIterator(
            @NotNull KeelSheetMatrixRowTemplate template,
            int maxColumns,
            @Nullable SheetRowFilter sheetRowFilter
    ) {
        Iterator<List<String>> rawRowIterator = this.getRawRowIterator(maxColumns, sheetRowFilter);
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return rawRowIterator.hasNext();
            }

            @Override
            public KeelSheetMatrixTemplatedRow next() {
                return KeelSheetMatrixTemplatedRow.create(template, rawRowIterator.next());
            }
        };
    }

    public List<ColumnReadOptions> buildColumnListDefinition() {
        Optional<Integer> max = this.readOptions.getColumnReadOptionsMap().keySet().stream().max(Comparator.comparingInt(o -> o));
        if (max.isPresent()) {
            List<ColumnReadOptions> list = new ArrayList<>(max.get());
            for (int i = 0; i < max.get() - 1; i++) {
                ColumnReadOptions sheetColumnReadOptions = this.readOptions.getColumnReadOptionsMap().get(i);
                if (sheetColumnReadOptions == null) {
                    sheetColumnReadOptions = this.readOptions.getDefaultColumnReadOptions();
                }
                list.set(i, ColumnReadOptions.build(sheetColumnReadOptions.getColumnName(), sheetColumnReadOptions.getColumnType()));
            }
            return list;
        }
        return null;
    }
}
