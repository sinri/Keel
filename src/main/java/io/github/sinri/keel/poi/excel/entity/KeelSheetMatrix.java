package io.github.sinri.keel.poi.excel.entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Excel → Matrix of Cells' String values → Customized Row Readers.
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
     * @since 3.0.14
     * @since 3.0.18 Finished Technical Preview.
     */
    public static class RowReaderIterator<R extends KeelSheetMatrixRow> implements Iterator<R> {
        //private final Class<R> rClass;
        private final Constructor<R> constructor;
        private final List<List<String>> rows;
        private final AtomicInteger ptr = new AtomicInteger(0);

        public RowReaderIterator(Class<R> rClass, List<List<String>> rows) {
            this.rows = rows;

            try {
                this.constructor = rClass.getConstructor(List.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean hasNext() {
            return this.rows.size() > ptr.get();
        }

        @Override
        public R next() {
            List<String> rawRow = this.rows.get(ptr.get());
            ptr.incrementAndGet();
            try {
                return this.constructor.newInstance(rawRow);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
