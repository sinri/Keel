package io.github.sinri.keel.mysql.matrix;

import io.github.sinri.keel.mysql.statement.DeleteStatement;
import io.github.sinri.keel.mysql.statement.UpdateStatement;
import io.github.sinri.keel.mysql.statement.WriteIntoStatement;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;

/**
 * @since 2.0
 */
public abstract class AbstractTableRow extends AbstractRow {
    public AbstractTableRow(JsonObject tableRow) {
        super(tableRow);
    }

    /**
     * @return default null
     */
    public String getSchemaName() {
        return null;
    }

    /**
     * @return table name
     */
    abstract public String getTableName();

    abstract public String getPKFiledName();

    public Future<Long> insertThisRowForPK(SqlConnection sqlConnection) {
        return new WriteIntoStatement()
                .intoTable(getSchemaName(), getTableName())
                .macroWriteOneRowWithJsonObject(getRow())
                .executeForLastInsertedID(sqlConnection);
    }

    public Future<Long> replaceThisRowForPK(SqlConnection sqlConnection) {
        return new WriteIntoStatement(WriteIntoStatement.REPLACE)
                .intoTable(getSchemaName(), getTableName())
                .macroWriteOneRowWithJsonObject(getRow())
                .executeForLastInsertedID(sqlConnection);
    }

    public Future<Integer> updateThisRow(SqlConnection sqlConnection) {
        UpdateStatement updateStatement = new UpdateStatement()
                .table(getSchemaName(), getTableName())
                .where(conditionsComponent -> conditionsComponent
                        .quickMapping(getPKFiledName(), getFieldAsNumber(getPKFiledName()).longValue()));
        this.getRow().forEach(entry -> {
            if (!entry.getKey().equals(getPKFiledName())) {
                if (entry.getValue() == null) {
                    updateStatement.setWithExpression(entry.getKey(), "NULL");
                } else {
                    updateStatement.setWithValue(entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
        });
        return updateStatement.limit(1).executeForAffectedRows(sqlConnection);
    }

    public Future<Integer> deleteThisRow(SqlConnection sqlConnection) {
        return new DeleteStatement()
                .from(getSchemaName(), getTableName())
                .where(conditionsComponent -> conditionsComponent
                        .quickMapping(getPKFiledName(), getFieldAsNumber(getPKFiledName()).longValue()))
                .limit(1)
                .executeForAffectedRows(sqlConnection);
    }
}
