package io.github.sinri.keel.excel.entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since 3.0.8
 */
public abstract class AbstractTemplatedColumnNameMapping implements TemplatedColumnNameMapping {
    private final Map<String, Integer> columnNameMap;
    private final List<String> columnNameList;

    public AbstractTemplatedColumnNameMapping(@Nonnull List<String> columnNameList) {
        if (columnNameList.isEmpty()) {
            throw new RuntimeException();
        }
        this.columnNameList = columnNameList;
        columnNameMap = new HashMap<>();
        for (int i = 0; i < columnNameList.size(); i++) {
            String s = columnNameList.get(i);
            if (s == null) continue;
            columnNameMap.put(s, i);
        }
    }

    public AbstractTemplatedColumnNameMapping(@Nonnull Map<String, Integer> columnNameMap) {
        if (columnNameMap.isEmpty()) {
            throw new RuntimeException();
        }
        this.columnNameMap = columnNameMap;
        AtomicInteger max = new AtomicInteger(-1);
        columnNameMap.forEach((k, v) -> {
            if (v == null || v < 0) throw new RuntimeException();
            if (v > max.get()) {
                max.set(v);
            }
        });
        String[] array = new String[max.get() + 1];
        for (int i = 0; i < max.get() + 1; i++) {
            array[i] = null;
        }
        columnNameMap.forEach((k, v) -> {
            if (v == null || v < 0) throw new RuntimeException();
            array[v] = k;
        });
        this.columnNameList = List.of(array);
    }

    public List<String> getColumnNameList() {
        return columnNameList;
    }

    public Map<String, Integer> getColumnNameMap() {
        return columnNameMap;
    }

    public String getColumnNameForIndex(int columnIndex) {
        return this.columnNameList.get(columnIndex);
    }

    public @Nullable Integer getColumnIndexForName(@Nonnull String columnName) {
        return this.columnNameMap.get(columnName);
    }
}
