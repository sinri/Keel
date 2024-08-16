package io.github.sinri.keel.mysql.matrix;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * @since 2.0
 * @since 2.8 rename and remove
 */
public abstract class AbstractTableRow extends SimpleResultRow {
    public AbstractTableRow(@NotNull JsonObject tableRow) {
        super(tableRow);
    }

    /**
     * @return default null
     */
    @Nullable
    public String sourceSchemaName() {
        return null;
    }

    /**
     * @return table name
     */
    @NotNull
    abstract public String sourceTableName();
}
