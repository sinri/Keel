package io.github.sinri.keel.test.sync;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.DuplexExecutor;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;

public class DataFetcherTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();
        DuplexExecutor<String> stringDuoFetcherWrapper = new DuplexExecutor<>();
        stringDuoFetcherWrapper
                .setAsyncExecutor(v -> Future.succeededFuture("async"))
                .setSyncExecutor(stringSyncExecuteResult -> {
                    try {
                        //stringSyncExecuteResult.setResult("sync");
                        throw new Exception("sync error");
                    } catch (Exception e) {
                        stringSyncExecuteResult.setError(e);
                    }
                    return stringSyncExecuteResult;
                });

        try {
            Keel.outputLogger("").info(stringDuoFetcherWrapper.executeSync());
        } catch (Exception e) {
            Keel.outputLogger("").error(e.getMessage());
        }

        stringDuoFetcherWrapper.executeAsync()
                .compose(s -> {
                    Keel.outputLogger("").info(s);
                    return Future.succeededFuture();
                })
                .eventually(v -> {
                    Keel.getVertx().close();
                    return Future.succeededFuture();
                });

    }
}
