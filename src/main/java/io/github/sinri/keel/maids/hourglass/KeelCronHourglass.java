package io.github.sinri.keel.maids.hourglass;

import io.github.sinri.keel.servant.sundial.KeelCronExpression;
import io.vertx.core.Handler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * It is designed as KeelSundial, to perform crontab in cluster.
 *
 * @since 2.9.3
 */
public class KeelCronHourglass extends KeelHourglassImpl {
    private final Map<String, List<Handler<Long>>> cronJobMap = new ConcurrentHashMap<>();

    private final Handler<Long> handler;

    public KeelCronHourglass(String hourglassName) {
        super(hourglassName);
        this.handler = now -> {
            Calendar calendar = new Calendar
                    .Builder()
                    .setInstant(now)
                    .build();

            List<Handler<Long>> cronJobs = getCronJobsWhenTriggered(calendar);
            cronJobs.forEach(cronJob -> cronJob.handle(now));
        };
    }

    public KeelHourglass updateCronJobs(KeelCronExpression keelCronExpression, List<Handler<Long>> cronJobList) {
        if (cronJobList == null) {
            this.cronJobMap.remove(keelCronExpression.getRawCronExpression());
        } else {
            this.cronJobMap.put(keelCronExpression.getRawCronExpression(), cronJobList);
        }
        return this;
    }

    protected List<Handler<Long>> getCronJobsWhenTriggered(Calendar calendar) {
        List<Handler<Long>> list = new ArrayList<>();
        cronJobMap.forEach((k, v) -> {
            if (new KeelCronExpression(k).match(calendar)) {
                list.addAll(v);
            }
        });
        return list;
    }

    @Override
    public long interval() {
        return 60_000L;
    }

    @Override
    public Handler<Long> regularHandler() {
        return handler;
    }
}
