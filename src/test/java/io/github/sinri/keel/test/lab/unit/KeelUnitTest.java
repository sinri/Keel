package io.github.sinri.keel.test.lab.unit;


import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.github.sinri.keel.tesuto.TestUnitResult;
import io.vertx.core.Future;

import javax.annotation.Nonnull;
import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.keel;

public class KeelUnitTest extends KeelTest {

    @Override
    protected @Nonnull Future<Void> starting() {
        keel.getConfiguration().loadPropertiesFile("config.properties");
        System.out.println("prepared");
        return Future.succeededFuture();
    }

    @TestUnit
    public Future<Void> test1() {
        System.out.println("test1");
        return KeelAsyncKit.sleep(1000L);
    }

    @TestUnit
    public Future<Void> test2() {
        System.out.println("test2");
        return KeelAsyncKit.sleep(2000L)
                .compose(v -> {
                    return Future.failedFuture(new RuntimeException("ddd"));
                });
    }

    @Nonnull
    @Override
    protected Future<Void> ending(List<TestUnitResult> testUnitResults) {
        System.out.println("cleaned with " + testUnitResults.size() + " results");
        return Future.succeededFuture();
    }
}
