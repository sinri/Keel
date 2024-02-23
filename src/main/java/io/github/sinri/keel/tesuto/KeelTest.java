package io.github.sinri.keel.tesuto;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.10
 */
abstract public class KeelTest {
    /**
     * @since 3.2.0
     */
    private static KeelEventLogger eventLogger;

    /**
     * It is designed to be called by the subclasses in develop environment (e.g. in IDE).
     */
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        String calledClass = System.getProperty("sun.java.command");

        eventLogger = KeelIssueRecordCenter.outputCenter().generateEventLogger("KeelTest");

        eventLogger.debug(r -> r.message("Keel Test Class: " + calledClass));

        Class<?> aClass = Class.forName(calledClass);

        eventLogger.debug(r -> r.message("Reflected Class: " + aClass));

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
            eventLogger.fatal(r -> r.message("At least one public method with @TestUnit is required."));
            System.exit(1);
        }

        VertxOptions vertxOptions = ((KeelTest) testInstance).buildVertxOptions();
        Keel.initializeVertxStandalone(vertxOptions);

        AtomicInteger totalPassedRef = new AtomicInteger();
        List<TestUnitResult> testUnitResults = new ArrayList<>();

        Future.succeededFuture()
                .compose(v -> {
                    eventLogger.info(r -> r.message("STARTING..."));
                    return ((KeelTest) testInstance).starting();
                })
                .compose(v -> {
                    eventLogger.info(r -> r.message("RUNNING TEST UNITS..."));

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
                                        eventLogger.info(r -> r.message("☐\tUNIT [" + testUnitResult.getTestName() + "] SKIPPED. Spent " + testUnitResult.getSpentTime() + " ms;"));
                                    } else {
                                        totalNonSkippedRef.incrementAndGet();
                                        if (!testUnitResult.isFailed()) {
                                            totalPassedRef.incrementAndGet();
                                            eventLogger.info(r -> r.message("☑︎\tUNIT [" + testUnitResult.getTestName() + "] PASSED. Spent " + testUnitResult.getSpentTime() + " ms;"));
                                        } else {
                                            eventLogger.error(r -> r.message("☒\tUNIT [" + testUnitResult.getTestName() + "] FAILED. Spent " + testUnitResult.getSpentTime() + " ms;"));
                                            eventLogger.exception(testUnitResult.getCause(), r -> r.message("CAUSED BY THIS"));
                                        }
                                    }
                                });
                                eventLogger.notice(r -> r.message("PASSED RATE: " + totalPassedRef.get() + " / " + totalNonSkippedRef.get() + " i.e. " + (100.0 * totalPassedRef.get() / totalNonSkippedRef.get()) + "%"));
                            });
                })
                .onFailure(throwable -> {
                    eventLogger.exception(throwable, r -> r.message("ERROR OCCURRED DURING TESTING"));
                })
                .eventually(() -> {
                    return ((KeelTest) testInstance).ending(testUnitResults);
                })
                .eventually(() -> {
                    return Keel.getVertx().close();
                });
    }

    protected @Nonnull VertxOptions buildVertxOptions() {
        return new VertxOptions();
    }

    /**
     * @since 3.2.0
     */
    protected KeelEventLogger getLogger() {
        return eventLogger;
    }

    /**
     * @since 3.2.0
     */
    protected void setLogger(KeelEventLogger eventLogger) {
        KeelTest.eventLogger = eventLogger;
    }

    protected @Nonnull Future<Void> starting() {
        return Future.succeededFuture();
    }

    protected @Nonnull Future<Void> ending(List<TestUnitResult> testUnitResults) {
        return Future.succeededFuture();
    }

}
