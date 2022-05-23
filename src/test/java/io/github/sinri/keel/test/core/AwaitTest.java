package io.github.sinri.keel.test.core;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.await.KeelAwaitCallback;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;
import io.vertx.sqlclient.SqlConnection;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutionException;

public class AwaitTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();
        Keel.getMySQLKit().getPool().withConnection(sqlConnection -> {
                    for (int i = 0; i < 10; i++) {
                        try {
                            WeakReference<SqlConnection> weakReferenceOfSqlConnection = new WeakReference<>(sqlConnection);
                            KeelAwaitCallback.awaitVertxFuture(
                                    Future.succeededFuture()
                                            .compose(v -> {
                                                SqlConnection sqlConnection1 = weakReferenceOfSqlConnection.get();
                                                if (sqlConnection1 == null) {
                                                    return Future.failedFuture("sql connection null!");
                                                }
                                                return sqlConnection1.query("select sleep(1);").execute();
                                            })
                            );
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println(i + " over");
                    }
                    return Future.succeededFuture();
                })
                .eventually(v -> {
                    return Keel.getVertx().close();
                });
    }
}
