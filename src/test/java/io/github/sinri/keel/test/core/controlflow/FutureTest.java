package io.github.sinri.keel.test.core.controlflow;

import io.github.sinri.keel.lagecy.Keel;
import io.github.sinri.keel.lagecy.core.logger.KeelLogger;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;

public class FutureTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v0 -> {
            KeelLogger logger = Keel.outputLogger("FutureTest");
            Future
                    .succeededFuture()
                    .compose(v -> {
                        return Future.failedFuture("0");
                    })
                    .compose(
                            v -> {
                                return Future.succeededFuture()
                                        .compose(vv -> {
                                            return Future.failedFuture("1");
                                        });
                            },
                            throwable -> {
                                logger.exception("compose 2nd param", throwable);
                                return Future.succeededFuture(false);
                            }
                    )
                    .onFailure(throwable -> {
                        logger.exception("on failure", throwable);
                    })
                    .onSuccess(b -> {
                        logger.info("on success " + b);
                    })
                    .eventually(v -> {
                        logger.info("FIN");
                        return Keel.getVertx().close();
                    });
        /*
        上面的实验可以得出结论：
        FutureInstance.compose(a,b)
        等价于
        FutureInstance.onComplete(r->{
            // if r failed -> b
            // a
        })
         */
        });

    }
}
