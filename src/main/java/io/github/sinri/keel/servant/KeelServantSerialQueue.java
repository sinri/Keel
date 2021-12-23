package io.github.sinri.keel.servant;

import io.vertx.core.Future;

abstract public class KeelServantSerialQueue {

    public KeelServantSerialQueue() {
    }

    public Future<Void> run() {
//        getNextTask().compose(task -> {
//            if (task == null) {
//                vertx.setTimer(1000, id -> run());
//            } else {
//                // execute task
//                task.execute().compose(feedback -> {
//                    System.out.println("KeelServantSerialQueue executed task: " + feedback);
//                    return Future.succeededFuture();
//                }).onFailure(throwable -> {
//                    System.out.println("KeelServantSerialQueue executed task but failed: " + throwable);
//                }).eventually(v -> {
//                    run();
//                    return Future.succeededFuture();
//                });
//            }
//            return Future.succeededFuture();
//        });

        getNextTask().compose(KeelServantQueueTask::execute)
                .onFailure(throwable -> {
                    whenExecuteFailed(throwable).compose(x -> run());
                })
                .onSuccess(feedback -> {
                    whenExecuteDone(feedback).compose(x -> run());
                });
        return Future.succeededFuture();
    }

    /**
     * @param throwable thrown by the execute method from task
     * @return a future with null as string is suggested
     */
    abstract public Future<String> whenExecuteFailed(Throwable throwable);

    abstract public Future<String> whenExecuteDone(String feedback);

    abstract public Future<KeelServantQueueTask> getNextTask();
}
