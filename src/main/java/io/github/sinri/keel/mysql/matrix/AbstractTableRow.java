package io.github.sinri.keel.mysql.matrix;

import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 2.0
 * @since 2.8 rename and remove
 */
public abstract class AbstractTableRow extends SimpleResultRow {
    public AbstractTableRow(@Nonnull JsonObject tableRow) {
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
    @Nonnull
    abstract public String sourceTableName();
}
