package io.github.sinri.keel.mysql.matrix;

import io.github.sinri.keel.mysql.statement.DeleteStatement;
import io.github.sinri.keel.mysql.statement.UpdateStatement;
import io.github.sinri.keel.mysql.statement.WriteIntoStatement;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.SqlConnection;

import java.util.Objects;

/**
 * @since 2.0
 * @since 2.8 rename and remove
 */
public abstract class AbstractTableRow extends SimpleResultRow {
    public AbstractTableRow(JsonObject tableRow) {
        super(tableRow);
    }

    /**
     * @return default null
     */
    public String sourceSchemaName() {
        return null;
    }

    @Deprecated(since = "2.8", forRemoval = true)
    public final String getSchemaName() {
        return sourceSchemaName();
    }

    /**
     * @return table name
     */
    abstract public String sourceTableName();

    @Deprecated(since = "2.8", forRemoval = true)
    public final String getTableName() {
        return sourceTableName();
    }

    @Deprecated(since = "2.8", forRemoval = true)
    protected String getPKFiledName() {
        return null;
    }

    @Deprecated(since = "2.8", forRemoval = true)
    protected Future<Long> insertThisRowForPK(SqlConnection sqlConnection) {
        return new WriteIntoStatement()
                .intoTable(sourceSchemaName(), sourceTableName())
                .macroWriteOneRowWithJsonObject(getRow())
                .executeForLastInsertedID(sqlConnection);
    }

    @Deprecated(since = "2.8", forRemoval = true)
    protected Future<Long> replaceThisRowForPK(SqlConnection sqlConnection) {
        Objects.requireNonNull(getPKFiledName());
        return new WriteIntoStatement(WriteIntoStatement.REPLACE)
                .intoTable(sourceSchemaName(), sourceTableName())
                .macroWriteOneRowWithJsonObject(getRow())
                .executeForLastInsertedID(sqlConnection);
    }

    @Deprecated(since = "2.8", forRemoval = true)
    protected Future<Integer> updateThisRow(SqlConnection sqlConnection) {
        Objects.requireNonNull(getPKFiledName());
        UpdateStatement updateStatement = new UpdateStatement()
                .table(sourceSchemaName(), sourceTableName())
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

    @Deprecated(since = "2.8", forRemoval = true)
    protected Future<Integer> deleteThisRow(SqlConnection sqlConnection) {
        Objects.requireNonNull(getPKFiledName());
        return new DeleteStatement()
                .from(sourceSchemaName(), sourceTableName())
                .where(conditionsComponent -> conditionsComponent
                        .quickMapping(getPKFiledName(), getFieldAsNumber(getPKFiledName()).longValue()))
                .limit(1)
                .executeForAffectedRows(sqlConnection);
    }
}
