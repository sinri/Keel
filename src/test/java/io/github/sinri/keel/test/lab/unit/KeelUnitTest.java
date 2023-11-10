package io.github.sinri.keel.test.lab.unit;


import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.github.sinri.keel.tesuto.TestUnitResult;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class KeelUnitTest extends KeelTest {

    @Override
    protected @NotNull Future<Void> starting() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
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

    @NotNull
    @Override
    protected Future<Void> ending(List<TestUnitResult> testUnitResults) {
        System.out.println("cleaned with " + testUnitResults.size() + " results");
        return Future.succeededFuture();
    }
}
