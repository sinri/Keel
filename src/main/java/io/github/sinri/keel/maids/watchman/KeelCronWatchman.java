package io.github.sinri.keel.maids.watchman;

import io.github.sinri.keel.core.KeelCronExpression;
import io.github.sinri.keel.facade.Keel3;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

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
        return Keel3.getVertx().deployVerticle(keelCronWatchman, new DeploymentOptions().setWorker(true));
    }

    private static Future<Void> operateCronTab(String asyncMapName, Supplier<Future<Void>> supplier) {
        return Keel3.getVertx().sharedData().getLock(asyncMapName)
                .compose(lock -> supplier.get()
                        .andThen(ar -> lock.release()));
    }

    public static Future<Void> addCronJobToAsyncMap(
            String asyncMapName,
            KeelCronExpression keelCronExpression,
            Class<? extends KeelWatchmanEventHandler> eventHandlerClass
    ) {
        return addCronJobToAsyncMap(asyncMapName, keelCronExpression.getRawCronExpression(), eventHandlerClass.getName());
    }

    public static Future<Void> addCronJobToAsyncMap(
            String asyncMapName,
            String keelCronExpression,
            String eventHandlerClassName
    ) {
        return operateCronTab(
                asyncMapName,
                () -> Keel3.getVertx().sharedData().getAsyncMap(asyncMapName)
                        .compose(asyncMap -> asyncMap.put(
                                keelCronExpression + "@" + eventHandlerClassName,
                                new JsonObject()
                                        .put("cron", keelCronExpression)
                                        .put("handler", eventHandlerClassName)
                        ))
        );
    }

    public static Future<Void> replaceAllCronJobToAsyncMap(String asyncMapName, Map<String, List<String>> newMap) {
        Map<Object, JsonObject> hashMap = new HashMap<>();
        newMap.forEach((cronExpression, classes) -> classes.forEach(classItem -> {
            String hash = cronExpression + "@" + classItem;

            hashMap.put(hash, new JsonObject()
                    .put("cron", cronExpression)
                    .put("handler", classItem));
        }));
        return operateCronTab(asyncMapName, () -> Keel3.getVertx().sharedData().getAsyncMap(asyncMapName)
                .compose(asyncMap -> {
                    return asyncMap.keys()
                            .compose(oldKeys -> {
                                Set<Object> newKeys = hashMap.keySet();

                                HashSet<Object> toDeleteHashSet = new HashSet<>(oldKeys);
                                toDeleteHashSet.removeAll(newKeys);

                                HashSet<Object> toAddHashSet = new HashSet<>(newKeys);
                                toAddHashSet.removeAll(oldKeys);

                                return CompositeFuture.all(
                                        KeelAsyncKit.iterativelyCall(
                                                toDeleteHashSet,
                                                hash -> asyncMap.remove(String.valueOf(hash))
                                                        .compose(v -> Future.succeededFuture())),
                                        KeelAsyncKit.iterativelyCall(
                                                toAddHashSet,
                                                hash -> asyncMap.put(hash, hashMap.get(hash))
                                                        .compose(v -> Future.succeededFuture()))
                                );
                            })
                            .compose(v -> Future.succeededFuture());
                })
        );
    }

    public static Future<Void> removeCronJobFromAsyncMap(
            String asyncMapName,
            KeelCronExpression keelCronExpression,
            Class<? extends KeelWatchmanEventHandler> eventHandlerClass
    ) {
        return removeCronJobFromAsyncMap(asyncMapName, keelCronExpression.getRawCronExpression(), eventHandlerClass.getName());
    }

    public static Future<Void> removeCronJobFromAsyncMap(
            String asyncMapName,
            String keelCronExpression,
            String eventHandlerClassName
    ) {
        return removeCronJobFromAsyncMap(asyncMapName, keelCronExpression + "@" + eventHandlerClassName);
    }

    public static Future<Void> removeCronJobFromAsyncMap(String asyncMapName, String hash) {
        return operateCronTab(
                asyncMapName,
                () -> Keel3.getVertx().sharedData().getAsyncMap(asyncMapName)
                        .compose(asyncMap -> asyncMap.remove(hash)
                                .compose(v -> Future.succeededFuture()))
        );
    }

    public static Future<Void> removeAllCronJobsFromAsyncMap(String asyncMapName) {
        return operateCronTab(
                asyncMapName,
                () -> Keel3.getVertx().sharedData().getAsyncMap(asyncMapName)
                        .compose(AsyncMap::clear));
    }

    public static Future<Map<String, List<String>>> getAllCronJobsFromAsyncMap(String asyncMapName) {
        return Keel3.getVertx().sharedData().getAsyncMap(asyncMapName)
                .compose(AsyncMap::entries)
                .compose(entries -> {
                    Map<String, List<String>> map = new HashMap<>();
                    entries.forEach((hash, v) -> {
                        JsonObject jsonObject = (JsonObject) v;
                        String cron = jsonObject.getString("cron");
                        String handler = jsonObject.getString("handler");
                        map.computeIfAbsent(cron, s -> new ArrayList<>())
                                .add(handler);
                    });
                    return Future.succeededFuture(map);
                });
    }

    public static Future<List<KeelWatchmanEventHandler>> readAsyncMapForEventHandlers(
            String asyncMapName,
            Calendar calendar
    ) {
        List<KeelWatchmanEventHandler> list = new ArrayList<>();
        return Keel3.getVertx().sharedData().getAsyncMap(asyncMapName)
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
                                    //Keel.outputLogger().exception(e);
                                    System.out.println("EXCEPTION: " + e);
                                }
                            });
                        }
                    });
                    return Future.succeededFuture(list);
                });
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
