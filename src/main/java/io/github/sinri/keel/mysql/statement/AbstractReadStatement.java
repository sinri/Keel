package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.NamedMySQLConnection;
import io.github.sinri.keel.mysql.matrix.ResultRow;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import java.util.List;
import java.util.Map;
import java.util.function.Function;


/**
 * @since 1.10
 */
public abstract class AbstractReadStatement extends AbstractStatement {

    /**
     * @param namedMySQLConnection
     * @param classT
     * @param <T>
     * @return
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    public <T extends ResultRow> Future<T> queryForOneRow(NamedMySQLConnection namedMySQLConnection, Class<T> classT) {
        return queryForOneRow(namedMySQLConnection.getSqlConnection(), classT);
    }

    /**
     * @param namedMySQLConnection
     * @param classT
     * @param <T>
     * @return
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    public <T extends ResultRow> Future<List<T>> queryForRowList(NamedMySQLConnection namedMySQLConnection, Class<T> classT) {
        return queryForRowList(namedMySQLConnection.getSqlConnection(), classT);
    }

    /**
     * @param namedMySQLConnection
     * @param classT
     * @param categoryGenerator
     * @param <K>
     * @param <T>
     * @return
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    public <K, T extends ResultRow> Future<Map<K, List<T>>> queryForCategorizedMap(
            NamedMySQLConnection namedMySQLConnection,
            Class<T> classT,
            Function<T, K> categoryGenerator
    ) {
        return queryForCategorizedMap(namedMySQLConnection.getSqlConnection(), classT, categoryGenerator);
    }

    /**
     * @param namedMySQLConnection
     * @param classT
     * @param uniqueKeyGenerator
     * @param <K>
     * @param <T>
     * @return
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    public <K, T extends ResultRow> Future<Map<K, T>> queryForUniqueKeyBoundMap(
            NamedMySQLConnection namedMySQLConnection,
            Class<T> classT,
            Function<T, K> uniqueKeyGenerator
    ) {
        return queryForUniqueKeyBoundMap(namedMySQLConnection.getSqlConnection(), classT, uniqueKeyGenerator);
    }

    /**
     * @param sqlConnection SqlConnection
     * @param classT        class of type of result object
     * @param <T>           type of result object
     * @return 查询到数据，异步返回第一行数据封装的指定类实例；查询不到时异步返回null。
     * @since 2.1
     */
    public <T extends ResultRow> Future<T> queryForOneRow(SqlConnection sqlConnection, Class<T> classT) {
        return ResultRow.fetchResultRow(sqlConnection, this, classT);
    }

    /**
     * @param sqlConnection SqlConnection
     * @param classT        class of type of result object
     * @param <T>           type of result object
     * @return 查询到数据，异步返回所有行数据封装的指定类实例；查询不到时异步返回null。
     * @since 2.1
     */
    public <T extends ResultRow> Future<List<T>> queryForRowList(SqlConnection sqlConnection, Class<T> classT) {
        return ResultRow.fetchResultRows(sqlConnection, this, classT);
    }

    /**
     * @since 3.0.0
     */
    public static AbstractReadStatement buildWithRawSQL(String sql) {
        return new AbstractReadStatement() {
            @Override
            public String toString() {
                return sql;
            }
        };
    }

    /**
     * @param sqlConnection
     * @param classT
     * @param categoryGenerator
     * @param <K>
     * @param <T>
     * @return
     * @since 2.9.4
     */
    public <K, T extends ResultRow> Future<Map<K, List<T>>> queryForCategorizedMap(
            SqlConnection sqlConnection,
            Class<T> classT,
            Function<T, K> categoryGenerator
    ) {
        return ResultRow.fetchResultRowsToCategorizedMap(sqlConnection, this, classT, categoryGenerator);
    }

    /**
     * @param sqlConnection
     * @param classT
     * @param uniqueKeyGenerator
     * @param <K>
     * @param <T>
     * @return
     * @since 2.9.4
     */
    public <K, T extends ResultRow> Future<Map<K, T>> queryForUniqueKeyBoundMap(
            SqlConnection sqlConnection,
            Class<T> classT,
            Function<T, K> uniqueKeyGenerator
    ) {
        return ResultRow.fetchResultRowsToUniqueKeyBoundMap(sqlConnection, this, classT, uniqueKeyGenerator);
    }
}
