package io.github.sinri.keel.tesuto;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since 3.0.10
 */
abstract public class KeelTest {
    /**
     * It is designed to be called by the subclasses.
     */
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String calledClass = System.getProperty("sun.java.command");

        KeelEventLogger logger = KeelOutputEventLogCenter.getInstance().createLogger("KeelTest");
        logger.debug("Keel Test Class: " + calledClass);

        Class<?> aClass = Class.forName(calledClass);

        logger.debug("Reflected Class: " + aClass);

        Constructor<?> constructor = aClass.getConstructor();
        var testInstance = constructor.newInstance();

        Method[] methods = aClass.getMethods();

        List<TestUnitWrapper> testUnits = new ArrayList<>();

        for (Method method : methods) {
            TestUnit annotation = method.getAnnotation(TestUnit.class);
            if (annotation == null) {
                continue;
            }
            if (!method.getReturnType().isAssignableFrom(Future.class)) {
                continue;
            }
            testUnits.add(new TestUnitWrapper(method, annotation));
        }

        if (testUnits.isEmpty()) {
            logger.fatal("At least one public method with @TestUnit is required.");
            System.exit(1);
        }

        VertxOptions vertxOptions = ((KeelTest) testInstance).buildVertxOptions();
        Keel.initializeVertxStandalone(vertxOptions);

        AtomicInteger totalPassedRef = new AtomicInteger();
        List<TestUnitResult> testUnitResults = new ArrayList<>();

        Future.succeededFuture()
                .compose(v -> {
                    logger.info("STARTING...");
                    return ((KeelTest) testInstance).starting();
                })
                .compose(v -> {
                    logger.info("RUNNING TEST UNITS...");

                    return KeelAsyncKit.iterativelyCall(testUnits, testUnit -> {
                                return testUnit.runTest((KeelTest) testInstance)
                                        .compose(testUnitResult -> {
                                            testUnitResults.add(testUnitResult);
                                            return Future.succeededFuture();
                                        });
                            })
                            .onComplete(vv -> {
                                AtomicInteger totalNonSkippedRef = new AtomicInteger(0);
                                testUnitResults.forEach(testUnitResult -> {
                                    if (testUnitResult.isSkipped()) {
                                        logger.info("☐\tUNIT [" + testUnitResult.getTestName() + "] SKIPPED. Spent " + testUnitResult.getSpentTime() + " ms;");
                                    } else {
                                        totalNonSkippedRef.incrementAndGet();
                                        if (!testUnitResult.isFailed()) {
                                            totalPassedRef.incrementAndGet();
                                            logger.info("☑︎\tUNIT [" + testUnitResult.getTestName() + "] PASSED. Spent " + testUnitResult.getSpentTime() + " ms;");
                                        } else {
                                            logger.error("☒\tUNIT [" + testUnitResult.getTestName() + "] FAILED. Spent " + testUnitResult.getSpentTime() + " ms;");
                                            logger.exception(testUnitResult.getCause());
                                        }
                                    }
                                });
                                logger.notice("PASSED RATE: " + totalPassedRef.get() + " / " + totalNonSkippedRef.get() + " i.e. " + (100.0 * totalPassedRef.get() / totalNonSkippedRef.get()) + "%");
                            });
                })
                .onFailure(throwable -> {
                    logger.exception(throwable, "ERROR OCCURRED DURING TESTING");
                })
                .eventually(v -> {
                    return ((KeelTest) testInstance).ending(testUnitResults);
                })
                .eventually(v -> {
                    return Keel.getVertx().close();
                });
    }

    protected @Nonnull VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    /**
     * Default as silent logger, override it to provide another implementation.
     *
     * @return the logger used in test process.
     * @since 3.0.17 the default test logger is to write into standard output.
     */
    protected @Nonnull KeelEventLogger logger() {
        return KeelOutputEventLogCenter.getInstance().createLogger(this.getClass().getName());
    }

    abstract protected @Nonnull Future<Void> starting();

    abstract protected @Nonnull Future<Void> ending(List<TestUnitResult> testUnitResults);

}
