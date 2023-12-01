package io.github.sinri.keel.poi.excel;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.vertx.core.Future;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
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

    public final void blockReadAllRows(@Nonnull Consumer<Row> rowConsumer) {
        Iterator<Row> it = getRowIterator();
//        System.out.println("blockReadAllRows before while");

        while (it.hasNext()) {
//            System.out.println("blockReadAllRows next!");
            Row row = it.next();
            rowConsumer.accept(row);
        }

//        System.out.println("blockReadAllRows after while");
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
