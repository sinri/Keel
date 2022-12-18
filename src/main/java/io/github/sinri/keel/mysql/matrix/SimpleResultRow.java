package io.github.sinri.keel.mysql.matrix;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * @since 1.10 Designed for a wrapper of each row in ResultMatrix
 * @since 2.0 renamed from AbstractTableRow
 * @since 2.7 renamed from AbstractRow
 */
public class SimpleResultRow implements ResultRow {
    private JsonObject row;

    public SimpleResultRow(JsonObject tableRow) {
        this.reloadDataFromJsonObject(tableRow);
    }

    @Override
    public final @NotNull JsonObject toJsonObject() {
        return row;
    }

    @Override
    public final @NotNull ResultRow reloadDataFromJsonObject(JsonObject jsonObject) {
        this.row = jsonObject;
        return this;
    }
}
