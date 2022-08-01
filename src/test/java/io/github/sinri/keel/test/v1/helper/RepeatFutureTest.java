package io.github.sinri.keel.test.v1.helper;


import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureFor;
import io.github.sinri.keel.core.controlflow.FutureForEach;
import io.github.sinri.keel.core.controlflow.FutureForRange;
import io.github.sinri.keel.core.controlflow.FutureRecursion;
import io.github.sinri.keel.mysql.exception.KeelSQLResultRowIndexError;
import io.github.sinri.keel.mysql.matrix.ResultMatrixWithVertx;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class RepeatFutureTest {
    protected static void testFutureFor(int limit) {
        SharedTestBootstrap.getMySQLKit()
                .withConnection(sqlConnection -> {
                    return FutureFor.call(
                            0, i -> i < limit, i -> i + 1,
                            k -> {
                                System.out.println("handle object " + k + " ...");
                                return sqlConnection.query("select (" + k + "*2) as y")
                                        .execute()
                                        .compose(rows -> Future.succeededFuture(new ResultMatrixWithVertx(rows)))
                                        .compose(resultMatrix -> {
                                            Long y = null;
                                            try {
                                                y = resultMatrix.getFirstRow().getLong("y");
                                            } catch (KeelSQLResultRowIndexError e) {
                                                return Future.failedFuture(e);
                                            }
                                            System.out.println("y=" + y);
                                            return Future.succeededFuture();
                                        });
                            }
                    );
                })
                .onFailure(throwable -> System.out.println("failed: " + throwable.getMessage()))
                .onSuccess(fin -> System.out.println("done: " + fin))
                .eventually(v -> {
                    return Keel.getVertx().close();
                });
    }

    protected static void testFutureForRange(int limit) {
        SharedTestBootstrap.getMySQLKit()
                .withConnection(sqlConnection -> {
                    return FutureForRange.call(
                            limit,
                            i -> {
                                System.out.println("handle object " + i + " ...");
                                return sqlConnection.query("select (" + i + "*2) as y")
                                        .execute()
                                        .compose(rows -> Future.succeededFuture(new ResultMatrixWithVertx(rows)))
                                        .compose(resultMatrix -> {
                                            Long y = null;
                                            try {
                                                y = resultMatrix.getFirstRow().getLong("y");
                                            } catch (KeelSQLResultRowIndexError e) {
                                                return Future.failedFuture(e);
                                            }
                                            System.out.println("y=" + y);
                                            return Future.succeededFuture();
                                        });
                            }
                    );
                })
                .onFailure(throwable -> System.out.println("failed: " + throwable.getMessage()))
                .onSuccess(fin -> System.out.println("done"))
                .eventually(v -> {
                    return Keel.getVertx().close();
                });
    }

    protected static void testFutureForeach(int limit) {
        List<Integer> list = new ArrayList<>();
        for (var i = 0; i < limit; i++) {
            list.add(i);
        }
        SharedTestBootstrap.getMySQLKit()
                .withConnection(sqlConnection -> {
                    return FutureForEach.call(
                            list,
                            item -> {
                                return sqlConnection.query("select (" + item + "*2) as x")
                                        .execute()
                                        .compose(rows -> Future.succeededFuture(new ResultMatrixWithVertx(rows)))
                                        .compose(resultMatrixWithVertx -> {
                                            try {
                                                Long y = resultMatrixWithVertx.getFirstRow().getLong("x");
                                                System.out.println("item=" + item + ";y=" + y);
                                                return Future.succeededFuture();
                                            } catch (KeelSQLResultRowIndexError e) {
                                                return Future.failedFuture(e);
                                            }
                                        });
                            }
                    );
                })
                .onFailure(throwable -> System.out.println("failed: " + throwable.getMessage()))
                .onSuccess(fin -> System.out.println("done: " + fin))
                .eventually(v -> {
                    return Keel.getVertx().close();
                });
    }

    public static Future<Integer> testRecursion(Future<Integer> future, Function<Integer, Future<Boolean>> shouldNextFunction) {
        return future.compose(lastInteger -> {
            return shouldNextFunction.apply(lastInteger)
                    .compose(shouldNext -> {
                        if (shouldNext) {
                            return testRecursion(Future.succeededFuture(lastInteger - 1), shouldNextFunction);
                        } else {
                            return Future.succeededFuture(lastInteger);
                        }
                    });
        });
    }

    public static void main(String[] args) {
        SharedTestBootstrap.initialize();
        int limit = 1000;

        //testFutureForeach(limit);

        FutureRecursion.call(
                        10,
                        x -> Future.succeededFuture(x > 0),
                        x -> {
                            System.out.println("now x=" + x);
                            return Future.succeededFuture(x - 1);
                        }
                )
                .compose(fin -> {
                    System.out.println("fin: " + fin);
                    return Future.succeededFuture();
                })
                .eventually(v -> {
                    Keel.getVertx().close();
                    return Future.succeededFuture();
                });
    }
}
