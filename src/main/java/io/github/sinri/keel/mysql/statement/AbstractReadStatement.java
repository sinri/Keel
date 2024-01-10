package io.github.sinri.keel.mysql.statement;

import io.github.sinri.keel.mysql.NamedMySQLConnection;
import io.github.sinri.keel.mysql.matrix.ResultRow;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


/**
 * @since 1.10
 */
public abstract class AbstractReadStatement extends AbstractStatement {

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    public <T extends ResultRow> Future<T> queryForOneRow(@Nonnull NamedMySQLConnection namedMySQLConnection,@Nonnull Class<T> classT) {
        return queryForOneRow(namedMySQLConnection.getSqlConnection(), classT);
    }

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    public <T extends ResultRow> Future<List<T>> queryForRowList(@Nonnull NamedMySQLConnection namedMySQLConnection, @Nonnull Class<T> classT) {
        return queryForRowList(namedMySQLConnection.getSqlConnection(), classT);
    }

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    public <K, T extends ResultRow> Future<Map<K, List<T>>> queryForCategorizedMap(
            @Nonnull NamedMySQLConnection namedMySQLConnection,
            @Nonnull Class<T> classT,
            @Nonnull Function<T, K> categoryGenerator
    ) {
        return queryForCategorizedMap(namedMySQLConnection.getSqlConnection(), classT, categoryGenerator);
    }

    /**
     * @since 3.0.11
     * @since 3.0.18 Finished Technical Preview.
     */
    public <K, T extends ResultRow> Future<Map<K, T>> queryForUniqueKeyBoundMap(
            @Nonnull NamedMySQLConnection namedMySQLConnection,
            @Nonnull Class<T> classT,
            @Nonnull Function<T, K> uniqueKeyGenerator
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
    public <T extends ResultRow> Future<T> queryForOneRow(@Nonnull SqlConnection sqlConnection, @Nonnull Class<T> classT) {
        return ResultRow.fetchResultRow(sqlConnection, this, classT);
    }

    /**
     * @param sqlConnection SqlConnection
     * @param classT        class of type of result object
     * @param <T>           type of result object
     * @return 查询到数据，异步返回所有行数据封装的指定类实例；查询不到时异步返回null。
     * @since 2.1
     */
    public <T extends ResultRow> Future<List<T>> queryForRowList(@Nonnull SqlConnection sqlConnection, @Nonnull Class<T> classT) {
        return ResultRow.fetchResultRows(sqlConnection, this, classT);
    }

    /**
     * @since 3.0.0
     */
    public static AbstractReadStatement buildWithRawSQL(@Nonnull String sql) {
        return new AbstractReadStatement() {
            @Override
            public String toString() {
                return sql;
            }
        };
    }

    /**
     * @since 2.9.4
     */
    public <K, T extends ResultRow> Future<Map<K, List<T>>> queryForCategorizedMap(
            @Nonnull SqlConnection sqlConnection,
            @Nonnull Class<T> classT,
            @Nonnull Function<T, K> categoryGenerator
    ) {
        return ResultRow.fetchResultRowsToCategorizedMap(sqlConnection, this, classT, categoryGenerator);
    }

    /**
     * @since 2.9.4
     */
    public <K, T extends ResultRow> Future<Map<K, T>> queryForUniqueKeyBoundMap(
            @Nonnull SqlConnection sqlConnection,
            @Nonnull Class<T> classT,
            @Nonnull Function<T, K> uniqueKeyGenerator
    ) {
        return ResultRow.fetchResultRowsToUniqueKeyBoundMap(sqlConnection, this, classT, uniqueKeyGenerator);
    }
}
