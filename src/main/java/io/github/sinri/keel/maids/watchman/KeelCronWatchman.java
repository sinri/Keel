package io.github.sinri.keel.maids.watchman;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.servant.sundial.KeelCronExpression;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.shareddata.AsyncMap;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.function.Function;

/**
 * It is designed as KeelSundial, to perform crontab in cluster.
 *
 * @since 2.9.3
 */
public class KeelCronWatchman extends KeelWatchmanImpl {

    private final KeelWatchmanEventHandler handler;
    private final Function<String, Future<Void>> cronTabUpdateStartup;

    protected KeelCronWatchman(String watchmanName, Function<String, Future<Void>> cronTabUpdateStartup) {
        super(watchmanName);
        this.handler = now -> {
            Calendar calendar = new Calendar
                    .Builder()
                    .setInstant(now)
                    .build();

            readAsyncMapForEventHandlers(calendar)
                    .onSuccess(list -> list.forEach(x -> x.handle(now)))
                    .onFailure(throwable -> getLogger().exception(throwable));
        };
        this.cronTabUpdateStartup = cronTabUpdateStartup;
    }

    public static Future<String> deploy(String watchmanName, Function<String, Future<Void>> cronTabUpdateStartup) {
        KeelCronWatchman keelCronWatchman = new KeelCronWatchman(watchmanName, cronTabUpdateStartup);
        return Keel.getVertx().deployVerticle(keelCronWatchman, new DeploymentOptions()
                .setWorker(true));
    }

    public static Future<Void> updateCronTabToAsyncMap(
            String asyncMapName,
            KeelCronExpression keelCronExpression,
            List<Class<? extends KeelWatchmanEventHandler>> cronJobList
    ) {
        return Keel.getVertx().sharedData().getAsyncMap(asyncMapName)
                .compose(asyncMap -> {
                    JsonArray array = new JsonArray();
                    cronJobList.forEach(c -> array.add(c.getName()));
                    return asyncMap.put(keelCronExpression.getRawCronExpression(), array);
                });
    }

    public static Future<List<KeelWatchmanEventHandler>> readAsyncMapForEventHandlers(
            String asyncMapName,
            Calendar calendar
    ) {
        List<KeelWatchmanEventHandler> list = new ArrayList<>();
        return Keel.getVertx().sharedData().getAsyncMap(asyncMapName)
                .compose(AsyncMap::entries)
                .compose(entries -> {
                    entries.forEach((k, v) -> {
                        String cronExpression = String.valueOf(k);
                        if (new KeelCronExpression(cronExpression).match(calendar)) {
                            JsonArray eventHandlerClassNameArray = new JsonArray(String.valueOf(v));
                            eventHandlerClassNameArray.forEach(eventHandlerClassName -> {
                                try {
                                    Class<?> aClass = Class.forName(String.valueOf(eventHandlerClassName));
                                    if (KeelWatchmanEventHandler.class.isAssignableFrom(aClass)) {
                                        KeelWatchmanEventHandler eventHandler = (KeelWatchmanEventHandler) aClass.getConstructor().newInstance();
                                        list.add(eventHandler);
                                    }
                                } catch (Throwable e) {
                                    Keel.outputLogger().exception(e);
                                }
                            });
                        }
                    });
                    return Future.succeededFuture(list);
                });
    }

    private Future<Void> updateCronTabToAsyncMap(KeelCronExpression keelCronExpression, List<Class<? extends KeelWatchmanEventHandler>> cronJobList) {
        return updateCronTabToAsyncMap(eventBusAddress(), keelCronExpression, cronJobList);
    }

    private Future<List<KeelWatchmanEventHandler>> readAsyncMapForEventHandlers(Calendar calendar) {
        return readAsyncMapForEventHandlers(eventBusAddress(), calendar);
    }

    @Override
    public final long interval() {
        return 60_000L;
    }

    @Override
    public final KeelWatchmanEventHandler regularHandler() {
        return handler;
    }

    @Override
    public void start() {
        Future.succeededFuture()
                .compose(v -> cronTabUpdateStartup.apply(eventBusAddress()))
                .onSuccess(v -> super.start())
                .onFailure(throwable -> {
                    getLogger().exception(throwable);
                    undeployMe();
                });
    }

}
