package io.github.sinri.keel.excel.entity;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * @since 3.0.8
 */
public interface TemplatedRow {

    @Nullable
    TemplatedColumnNameMapping getColumnNameMapping();

    @Nullable
    String readColumn(int columnNo);

    default void writeColumn(int columnNo, @Nullable String value) {
        this.getRowAsList().set(columnNo, value);
    }

    default void writeColumn(String columnName, @Nullable String value) {
        if (getColumnNameMapping() == null)
            throw new UnsupportedOperationException("LACK OF TemplatedColumnNameMapping");
        int columnIndexForName = Objects.requireNonNull(this.getColumnNameMapping().getColumnIndexForName(columnName));
        this.getRowAsList().set(columnIndexForName, value);
    }

    @Nullable
    default String readColumn(String columnName) {
        if (this.getColumnNameMapping() == null)
            throw new UnsupportedOperationException("LACK OF TemplatedColumnNameMapping");
        int columnIndexForName = Objects.requireNonNull(this.getColumnNameMapping().getColumnIndexForName(columnName));
        return readColumn(columnIndexForName);
    }

    List<String> getRowAsList();

    default JsonArray toJsonArray() {
        return new JsonArray(getRowAsList());
    }

    default JsonObject toJsonObject() {
        var columnNameMapping = this.getColumnNameMapping();
        if (columnNameMapping == null)
            throw new UnsupportedOperationException("LACK OF TemplatedColumnNameMapping");

        var rowAsList = getRowAsList();
        JsonObject row = new JsonObject();
        for (int i = 0; i < rowAsList.size(); i++) {
            String columnNameForIndex = columnNameMapping.getColumnNameForIndex(i);
            if (columnNameForIndex == null) {
                continue;
            }
            row.put(columnNameMapping.getColumnNameForIndex(i), rowAsList.get(i));
        }
        return row;
    }
}
