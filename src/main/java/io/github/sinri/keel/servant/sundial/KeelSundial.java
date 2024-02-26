package io.github.sinri.keel.servant.sundial;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.verticles.KeelVerticleImplWithEventLogger;
import io.vertx.core.Future;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.0
 */
public abstract class KeelSundial extends KeelVerticleImplWithEventLogger {
    private final Map<String, KeelSundialPlan> planMap = new ConcurrentHashMap<>();
    private Long timerID;
    private final AtomicInteger planFetchingSemaphore = new AtomicInteger(0);


    @Override
    protected KeelEventLogger buildEventLogger() {
        return KeelIssueRecordCenter.outputCenter().generateEventLogger("Sundial");
    }

    @Override

    public void start() throws Exception {
        long delaySeconds = 60 - (System.currentTimeMillis() / 1000) % 60;
        this.timerID = Keel.getVertx().setPeriodic(delaySeconds, 60_000L, timerID -> {
            Calendar calendar = Calendar.getInstance();
            handleEveryMinute(calendar);
        });
    }

    private void handleEveryMinute(Calendar now) {
        planMap.forEach((key, plan) -> {
            if (plan.cronExpression().match(now)) {
                plan.execute(now);
            }
        });

        // refresh plan, pfs {0: not-fetching, more: fetching}
        if (planFetchingSemaphore.get() == 0) {
            planFetchingSemaphore.incrementAndGet();
            fetchPlans()
                    .compose(plans -> {
                        if (plans == null) {
                            // treat null as NOT MODIFIED
                            return Future.succeededFuture();
                        }
                        Set<String> toDelete = new HashSet<>(planMap.keySet());
                        plans.forEach(plan -> {
                            toDelete.remove(plan.key());
                            planMap.put(plan.key(), plan);
                        });
                        if (!toDelete.isEmpty()) {
                            toDelete.forEach(planMap::remove);
                        }
                        return Future.succeededFuture();
                    })
                    .eventually(() -> {
                        planFetchingSemaphore.decrementAndGet();
                        return Future.succeededFuture();
                    });

        }

    }

    @Deprecated(since = "3.0.1", forRemoval = true)
    protected Supplier<Collection<KeelSundialPlan>> plansSupplier() {
        return null;
    }

    /**
     * @since 3.0.1
     * Before plansSupplier is removed, when plansSupplier returns non-null supplier, use that and ignore this.
     * If future as null, means `NOT MODIFIED`.
     */
    abstract protected Future<Collection<KeelSundialPlan>> fetchPlans();

    @Override
    public void stop() throws Exception {
        super.stop();
        if (this.timerID != null) {
            Keel.getVertx().cancelTimer(this.timerID);
        }
    }

}
