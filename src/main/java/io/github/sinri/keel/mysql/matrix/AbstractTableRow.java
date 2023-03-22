package io.github.sinri.keel.mysql.matrix;

import io.vertx.core.json.JsonObject;

/**
 * @since 2.0
 * @since 2.8 rename and remove
 */
public abstract class AbstractTableRow extends SimpleResultRow {
    public AbstractTableRow(JsonObject tableRow) {
        super(tableRow);
    }

    /**
     *Returns default null.
 
     */
    public String sourceSchemaName() {
        return null;
    }

    /**
     *Returns table name.
 
     */
    abstract public String sourceTableName();
}
