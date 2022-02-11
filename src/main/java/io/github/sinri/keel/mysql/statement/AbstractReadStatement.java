package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.MySQLExecutor;
import io.github.sinri.keel.mysql.jdbc.KeelJDBCForMySQL;
import io.github.sinri.keel.mysql.matrix.AbstractTableRow;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;


/**
 * @since 1.10
 */
public abstract class AbstractReadStatement extends AbstractStatement {
    @Override
    public ResultMatrix blockedExecute(Statement statement) throws SQLException {
        return KeelJDBCForMySQL.queryForSelection(this.toString(), statement);
    }

    public <T extends AbstractTableRow> MySQLExecutor<T> getOneTableRowFetcher(Class<T> classOfTableRow) {
        return AbstractTableRow.buildTableRowFetcher(this, classOfTableRow);
    }

    public <T extends AbstractTableRow> MySQLExecutor<List<T>> getTableRowListFetcher(Class<T> classOfTableRow) {
        return AbstractTableRow.buildTableRowListFetcher(this, classOfTableRow);
    }
}
