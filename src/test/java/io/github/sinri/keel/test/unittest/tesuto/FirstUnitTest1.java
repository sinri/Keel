package io.github.sinri.keel.test.unittest.tesuto;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.tesuto.KeelUnitTest;

public class FirstUnitTest1 extends KeelUnitTest {
    @Override
    protected void prepareEnvironment() {
        super.prepareEnvironment();
        getLogger().debug("io.github.sinri.keel.test.unittest.tesuto.FirstUnitTest.prepareEnvironment");
    }

    @Override
    public void setUp() {
        super.setUp();
        getLogger().debug("io.github.sinri.keel.test.unittest.tesuto.FirstUnitTest.setUp");
    }

    @Override
    public void tearDown() {
        super.tearDown();
        getLogger().debug("io.github.sinri.keel.test.unittest.tesuto.FirstUnitTest.tearDown");
    }

    public void test1() {
        getLogger().info("io.github.sinri.keel.test.unittest.tesuto.FirstUnitTest.test1");
    }

    public void test2() {
        getLogger().info("io.github.sinri.keel.test.unittest.tesuto.FirstUnitTest.test2 start");
//        KeelAsyncKit.pseudoAwait(p -> {
//            KeelAsyncKit.sleep(1000L)
//                    .andThen(ar -> {
//                        if (ar.succeeded()) {
//                            p.complete();
//                        } else {
//                            p.fail(ar.cause());
//                        }
//                    });
//        });
        async(() -> {
            return KeelAsyncKit.sleep(1000L);
        });
        getLogger().info("io.github.sinri.keel.test.unittest.tesuto.FirstUnitTest.test2 end");
    }
}
