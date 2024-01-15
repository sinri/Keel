package io.github.sinri.keel.poi.excel.entity;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Excel → Matrix of Cells' String values → Customized Row Readers.
 *
 * @since 3.0.13
 * @since 3.0.18 Finished Technical Preview.
 */
public class KeelSheetMatrix {
    private final List<String> headerRow;
    private final List<List<String>> rows;

    public KeelSheetMatrix() {
        this.headerRow = new ArrayList<>();
        this.rows = new ArrayList<>();
    }

    public KeelSheetMatrix addRow(List<String> row) {
        this.rows.add(row);
        return this;
    }

    public KeelSheetMatrix addRows(List<List<String>> rows) {
        this.rows.addAll(rows);
        return this;
    }

    public List<String> getHeaderRow() {
        return headerRow;
    }

    public KeelSheetMatrix setHeaderRow(List<String> headerRow) {
        this.headerRow.clear();
        this.headerRow.addAll(headerRow);
        return this;
    }

    public List<String> getRawRow(int i) {
        return this.rows.get(i);
    }

    public List<List<String>> getRawRowList() {
        return rows;
    }

    public KeelSheetTemplatedMatrix transformToTemplatedMatrix() {
        List<String> x = getHeaderRow();
        if (x.isEmpty()) throw new RuntimeException("Columns not defined");
        KeelSheetTemplatedMatrix templatedMatrix = KeelSheetTemplatedMatrix.create(KeelSheetMatrixRowTemplate.create(x));
        templatedMatrix.addRawRows(getRawRowList());
        return templatedMatrix;
    }

    public <R extends KeelSheetMatrixRow> Iterator<R> getRowIterator(Class<R> rClass) {
        return new RowReaderIterator<>(rClass, rows);
    }

    /**
     * @since 3.1.1
     */
    public Iterator<KeelSheetMatrixRow> getRowIterator() {
        return new RowReaderIterator<>(strings -> new KeelSheetMatrixRow(strings) {
        }, rows);
    }

    /**
     * @since 3.0.14
     * @since 3.0.18 Finished Technical Preview.
     */
    public static class RowReaderIterator<R extends KeelSheetMatrixRow> implements Iterator<R> {
        private final List<List<String>> rows;
        private final AtomicInteger ptr = new AtomicInteger(0);
        private final Function<List<String>, R> rawRow2row;

        public RowReaderIterator(Class<R> rClass, List<List<String>> rows) {
            this(strings -> {
                try {
                    var constructor = rClass.getConstructor(List.class);
                    return constructor.newInstance(strings);
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                         IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }, rows);
        }

        /**
         * @param rawRow2row A function to transform a Raw Row to a KeelSheetMatrixRow instance.
         * @param rows       the raw rows
         * @since 3.1.1
         */
        public RowReaderIterator(Function<List<String>, R> rawRow2row, List<List<String>> rows) {
            this.rawRow2row = rawRow2row;
            this.rows = rows;
        }

        @Override
        public boolean hasNext() {
            return this.rows.size() > ptr.get();
        }

        @Override
        public R next() {
            List<String> rawRow = this.rows.get(ptr.get());
            ptr.incrementAndGet();
            return this.rawRow2row.apply(rawRow);
        }
    }
}
