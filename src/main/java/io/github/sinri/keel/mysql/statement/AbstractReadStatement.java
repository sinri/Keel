package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.jdbc.KeelJDBCForMySQL;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;

import java.sql.SQLException;
import java.sql.Statement;

/**
 * @since 1.10
 */
public abstract class AbstractReadStatement extends AbstractStatement {
    @Override
    public ResultMatrix blockedExecute(Statement statement) throws SQLException {
        return KeelJDBCForMySQL.queryForSelection(this.toString(), statement);
    }
}
