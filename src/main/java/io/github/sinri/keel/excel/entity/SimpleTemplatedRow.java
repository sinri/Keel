package io.github.sinri.keel.excel.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @since 3.0.8
 */
public class SimpleTemplatedRow implements TemplatedRow {
    private final List<String> rowAsList;
    private @Nullable TemplatedColumnNameMapping columnNameMapping = null;

    public SimpleTemplatedRow(@Nonnull List<String> rowAsList, @Nullable TemplatedColumnNameMapping columnNameMapping) {
        this.rowAsList = rowAsList;
        this.columnNameMapping = columnNameMapping;
    }

    public SimpleTemplatedRow(@Nonnull Map<String, String> rowAsMap, @Nonnull TemplatedColumnNameMapping columnNameMapping) {
        this.columnNameMapping = columnNameMapping;
        int size = this.columnNameMapping.getColumnNameList().size();
        String[] array = new String[size];
        for (int i = 0; i < size; i++) {
            String columnNameForIndex = columnNameMapping.getColumnNameForIndex(i);
            if (columnNameForIndex == null) {
                array[i] = null;
            } else {
                array[i] = rowAsMap.get(columnNameForIndex);
            }
        }
        this.rowAsList = new ArrayList<>(List.of(array));
    }

    public SimpleTemplatedRow(@Nonnull TemplatedColumnNameMapping columnNameMapping, @Nullable Integer columnCount) {
        this.columnNameMapping = columnNameMapping;

        int minSize = columnNameMapping.getColumnNameList().size();

        this.rowAsList = new ArrayList<>();
        if (columnCount == null) {
            for (int i = 0; i < minSize; i++) {
                this.rowAsList.add(null);
            }
        } else {
            for (int i = 0; i < Math.max(minSize, columnCount); i++) {
                this.rowAsList.add(null);
            }
        }
    }

    public SimpleTemplatedRow(@Nonnull TemplatedColumnNameMapping columnNameMapping) {
        this(columnNameMapping, null);
    }

    public static TemplatedRow transformer(List<String> rawRow) {
        return new SimpleTemplatedRow(rawRow, null);
    }

    public @Nullable TemplatedColumnNameMapping getColumnNameMapping() {
        return columnNameMapping;
    }

    public @Nullable String readColumn(int columnNo) {
        return this.rowAsList.get(columnNo);
    }

    public List<String> getRowAsList() {
        return rowAsList;
    }
}
