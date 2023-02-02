package io.github.sinri.keel.servant.sundial;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.verticles.KeelVerticleBase;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @since 3.0.0
 */
public abstract class KeelSundial extends KeelVerticleBase {
    private final Map<String, KeelSundialPlan> planMap = new ConcurrentHashMap<>();
    private Long timerID;

    @Override

    public void start() throws Exception {
        super.start();
        setLogger(KeelOutputEventLogCenter.getInstance().createLogger(getClass().getName()));

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

        // refresh plan
        Set<String> toDelete = new HashSet<>(planMap.keySet());
        plansSupplier().get().forEach(plan -> {
            toDelete.remove(plan.key());
            planMap.put(plan.key(), plan);
        });
        if (!toDelete.isEmpty()) {
            toDelete.forEach(planMap::remove);
        }
    }

    abstract protected Supplier<Collection<KeelSundialPlan>> plansSupplier();

    @Override
    public void stop() throws Exception {
        super.stop();
        if (this.timerID != null) {
            Keel.getVertx().cancelTimer(this.timerID);
        }
    }

}
