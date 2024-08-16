package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.NamedMySQLConnection;
import io.github.sinri.keel.mysql.matrix.ResultMatrix;
import io.github.sinri.keel.mysql.statement.templated.TemplateArgumentMapping;
import io.github.sinri.keel.mysql.statement.templated.TemplatedModifyStatement;
import io.github.sinri.keel.mysql.statement.templated.TemplatedReadStatement;
import io.github.sinri.keel.mysql.statement.templated.TemplatedStatement;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.sqlclient.SqlConnection;
import org.jetbrains.annotations.NotNull;


/**
 * @since 3.0.9
 */
public interface AnyStatement {


    /**
     * @since 3.0.9
     */
    static AbstractStatement raw(@NotNull String sql) {
        return new AbstractStatement() {
            @Override
            public String toString() {
                return sql;
            }
        };
    }

    static SelectStatement select(@NotNull Handler<SelectStatement> statementHandler) {
        SelectStatement selectStatement = new SelectStatement();
        statementHandler.handle(selectStatement);
        return selectStatement;
    }

    static UnionStatement union(@NotNull Handler<UnionStatement> unionStatementHandler) {
        UnionStatement unionStatement = new UnionStatement();
        unionStatementHandler.handle(unionStatement);
        return unionStatement;
    }

    static UpdateStatement update(@NotNull Handler<UpdateStatement> updateStatementHandler) {
        UpdateStatement updateStatement = new UpdateStatement();
        updateStatementHandler.handle(updateStatement);
        return updateStatement;
    }

    static DeleteStatement delete(@NotNull Handler<DeleteStatement> deleteStatementHandler) {
        DeleteStatement deleteStatement = new DeleteStatement();
        deleteStatementHandler.handle(deleteStatement);
        return deleteStatement;
    }

    static WriteIntoStatement insert(Handler<WriteIntoStatement> statementHandler) {
        WriteIntoStatement writeIntoStatement = new WriteIntoStatement(WriteIntoStatement.INSERT);
        statementHandler.handle(writeIntoStatement);
        return writeIntoStatement;
    }

    static WriteIntoStatement replace(@NotNull Handler<WriteIntoStatement> statementHandler) {
        WriteIntoStatement writeIntoStatement = new WriteIntoStatement(WriteIntoStatement.REPLACE);
        statementHandler.handle(writeIntoStatement);
        return writeIntoStatement;
    }

    static TemplatedReadStatement templatedRead(@NotNull String path, @NotNull Handler<TemplateArgumentMapping> templatedReadStatementHandler) {
        TemplatedReadStatement readStatement = TemplatedStatement.loadTemplateToRead(path);
        TemplateArgumentMapping arguments = readStatement.getArguments();
        templatedReadStatementHandler.handle(arguments);
        return readStatement;
    }

    static TemplatedModifyStatement templatedModify(@NotNull String path, @NotNull Handler<TemplateArgumentMapping> templatedModifyStatementHandler) {
        TemplatedModifyStatement templatedModifyStatement = TemplatedStatement.loadTemplateToModify(path);
        TemplateArgumentMapping arguments = templatedModifyStatement.getArguments();
        templatedModifyStatementHandler.handle(arguments);
        return templatedModifyStatement;
    }

    /**
     * @return The SQL Generated
     */
    String toString();

    Future<ResultMatrix> execute(@NotNull SqlConnection sqlConnection);

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    default Future<ResultMatrix> execute(@NotNull NamedMySQLConnection namedSqlConnection) {
        return execute(namedSqlConnection.getSqlConnection());
    }
}
