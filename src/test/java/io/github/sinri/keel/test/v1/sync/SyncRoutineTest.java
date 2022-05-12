package io.github.sinri.keel.test.v1.sync;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.await.KeelAwait;
import io.github.sinri.keel.mysql.statement.SelectStatement;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;

public class SyncRoutineTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

//        try {
//            String awaitedResult= new KeelAwaitV1<>(v -> {
//                try {
//                    Thread.sleep(300);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                return Future.succeededFuture("ASYNC-RESULT");
//            }).execute();
//            System.out.println("awaitedResult: " + awaitedResult);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }

        Keel.getVertx().setTimer(5000, timer -> {
            Keel.outputLogger("SyncRoutineTest").info("action 1 done");
        });

        Keel.getVertx().setTimer(3000, timer -> {
            Keel.outputLogger("SyncRoutineTest").info("action 2 done");
        });

//        Future<Object> f1 = Future.succeededFuture()
//                .compose(v -> {
//                    Keel.outputLogger("SyncRoutineTest").info("before action start");
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    return Future.succeededFuture();
//                })
//                .compose(v -> {
//                    Keel.outputLogger("SyncRoutineTest").info("before action end");
//                    return Future.succeededFuture();
//                });

//        Future<Object> f1b = Future.succeededFuture()
//                .compose(v -> {
//                    Keel.outputLogger("SyncRoutineTest").info("before b action start");
//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    return Future.succeededFuture();
//                })
//                .compose(v -> {
//                    Keel.outputLogger("SyncRoutineTest").info("before b action end");
//                    return Future.succeededFuture();
//                });

        try {
            Keel.outputLogger("SyncRoutineTest").info("start await");
            String executed = KeelAwait.asyncExecute(
                    v -> {
                        return SharedTestBootstrap.getMySQLKit().queryInConnection(
                                new SelectStatement().columnAsExpression("sleep(3)")
                        ).compose(resultMatrix -> {
                            return Future.succeededFuture("ASYNC-RESULT");
                        });
                    },
                    1000L
            );
            Keel.outputLogger("SyncRoutineTest").info("end await: " + executed);
        } catch (Throwable e) {
            Keel.outputLogger("SyncRoutineTest").exception(e);
        }

        Keel.getVertx().setTimer(3000, timer -> {
            Keel.outputLogger("SyncRoutineTest").info("action 3 done");
        });

//        Future<Object> f2 = Future.succeededFuture()
//                .compose(v -> {
//                    Keel.outputLogger("SyncRoutineTest").info("after action start");
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    return Future.succeededFuture();
//                })
//                .compose(v -> {
//                    Keel.outputLogger("SyncRoutineTest").info("after action end");
//                    return Future.succeededFuture();
//                });
//
//
//        CompositeFuture.join(f1,f2).eventually(v->{
//            Keel.getVertx().close();
//            return Future.succeededFuture();
//        });

        Keel.getVertx().setTimer(7000, timer -> {
            Keel.outputLogger("SyncRoutineTest").info("close vertx");
            Keel.getVertx().close();
        });

    }


}

