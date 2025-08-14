package io.github.sinri.keel.test.lab.mysql;

import io.github.sinri.keel.mysql.DynamicNamedMySQLConnection;
import io.github.sinri.keel.mysql.KeelMySQLDataSourceProvider;
import io.github.sinri.keel.mysql.NamedMySQLDataSource;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;

import javax.annotation.Nonnull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class QuickQueryTest extends KeelTest {
    @Nonnull
    @Override
    protected Future<Void> starting() {
        return super.starting()
                    .compose(v -> {
                        Keel.getConfiguration().loadPropertiesFile("config.properties");
                        return Future.succeededFuture();
                    });
    }

    @TestUnit(skip = true)
    public Future<Void> test1() {
        NamedMySQLDataSource<DynamicNamedMySQLConnection> cs = KeelMySQLDataSourceProvider.initializeDynamicNamedMySQLDataSource("cs");
        return cs.getConfiguration().instantQueryForStreamWithCursor(
                         "select * from cornerstone.file where file_id<0 limit 10",
                         1 + 1,
                         rowList -> {
                             RowIterator<Row> iterator = rowList.iterator();
                             while (iterator.hasNext()) {
                                 Row next = iterator.next();
                                 getLogger().info("row: " + next.getLong("file_id"));
                             }
                             return Future.succeededFuture();
                         })
                 .compose(v -> {
                     getLogger().info("fin");
                     return Future.succeededFuture();
                 });
    }

    @TestUnit(skip = true)
    public Future<Void> test2() {
        NamedMySQLDataSource<DynamicNamedMySQLConnection> cs = KeelMySQLDataSourceProvider.initializeDynamicNamedMySQLDataSource("cs");
        return cs.getConfiguration().instantQuery("select * from cornerstone.file limit 10")
                 .compose(resultMatrix -> {
                     getLogger().info("resultMatrix: " + resultMatrix.toJsonArray());
                     return Future.succeededFuture();
                 });
    }

    //    @TestUnit
    //    public Future<Void> test3() {
    //        NamedMySQLDataSource<DynamicNamedMySQLConnection> cs = KeelMySQLDataSourceProvider.initializeDynamicNamedMySQLDataSource("pioneer");
    //        AtomicInteger count = new AtomicInteger(5);
    //        return cs.getConfiguration().instantQueryForStreamV2(
    //                         "select * from tiberias.tiberias_dim_warehouse limit 10",
    //                         row -> {
    //                             getLogger().info("row: " + row.getLong("dim_wh_id"));
    //                             if (count.decrementAndGet() > 0) {
    //                                 return Future.succeededFuture();
    //                             } else {
    //                                 throw new RuntimeException("count down to heaven");
    //                             }
    //                         })
    //                 .compose(v -> {
    //                     getLogger().info("fin");
    //                     return Future.succeededFuture();
    //                 });
    //    }
}
