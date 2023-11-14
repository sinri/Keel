package io.github.sinri.keel.excel.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

abstract public class AbstractTemplatedRow<M extends TemplatedColumnNameMapping> implements TemplatedRow {
    private final List<String> rowAsList;
    private @Nullable M columnNameMapping = null;

    public AbstractTemplatedRow(@Nonnull List<String> rowAsList, @Nullable M columnNameMapping) {
        this.rowAsList = rowAsList;
        this.columnNameMapping = columnNameMapping;
    }

    public AbstractTemplatedRow(@Nonnull Map<String, String> rowAsMap, @Nonnull M columnNameMapping) {
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

    public AbstractTemplatedRow(@Nonnull M columnNameMapping, @Nullable Integer columnCount) {
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

    public AbstractTemplatedRow(@Nonnull M columnNameMapping) {
        this(columnNameMapping, null);
    }

    public @Nullable M getColumnNameMapping() {
        return columnNameMapping;
    }

    public @Nullable String readColumn(int columnNo) {
        return this.rowAsList.get(columnNo);
    }

    public List<String> getRowAsList() {
        return rowAsList;
    }
}
