package io.github.sinri.keel.servant.maxim;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Lock;

/**
 * 用于集群模式下的零散任务。
 *
 * @since 2.9
 */
public class KeelMaxim {
    private final String maximName;
    private KeelLogger logger;

    public KeelMaxim(String maximName) {
        this.maximName = maximName;
        this.logger = KeelLogger.silentLogger();
    }

    public static Future<Void> fire(MaximBullet bullet) {
        try {
            MaximBullet maximBullet = (MaximBullet) bullet.getImplementClass().getConstructor().newInstance();
            maximBullet.reloadDataFromJsonObject(bullet.toJsonObject());
            return maximBullet.fire();
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }
    }

    public KeelLogger getLogger() {
        return logger;
    }

    public KeelMaxim setLogger(KeelLogger logger) {
        this.logger = logger;
        return this;
    }

    private Future<AsyncMap<String, MaximBullet>> getAsyncMap() {
        return Keel.getVertx().sharedData().getAsyncMap(maximName + "-AsyncMap");
    }

    private Future<Lock> getBulletLock(String key) {
        return Keel.getVertx().sharedData().getLock(maximName + "-lock-" + key);
    }

    private Future<Void> product(MaximBullet bullet) {
        return getAsyncMap().compose(asyncMap -> {
            getLogger().info("PRODUCT: ", bullet.toJsonObject());
            return asyncMap.put(bullet.getID(), bullet);
        });
    }

    private Future<Void> consume() {
        return this.getAsyncMap().compose(map -> map.keys().compose(keys -> {
                    for (var key : keys) {
                        getLogger().info("CONSUME KEY: " + key);
                        return getBulletLock(String.valueOf(key)).compose(lock -> map.remove(key)
                                .compose(maximBullet -> {
                                    getLogger().info("TOOK OFF ENTRY WITH KEY: " + key);
                                    lock.release();
                                    if (maximBullet == null) {
                                        getLogger().warning("MAXIM BULLET IS NULL FOR KEY: " + key);
                                        return Future.succeededFuture();
                                    } else {
                                        getLogger().info("TO FIRE MAXIM BUFFET WITH KEY: " + key);
                                        return fire(maximBullet);
                                    }
                                })
                        );
                    }
                    return Future.succeededFuture();
                }))
                .compose(fired -> {
                    getLogger().info("MAXIM BULLET FIRED");
                    return Future.succeededFuture();
                }, throwable -> {
                    getLogger().exception("MAXIM BULLET JAMMED", throwable);
                    return Future.succeededFuture();
                });
    }

    public void runAsConsumer() {
        routine().andThen(ar -> {
            Keel.getVertx().setTimer(1000L, timerID -> {
                routine();
            });
        });
    }

    private MessageConsumer<JsonObject> consumer;

    public void runAsProducer() {
        this.consumer = Keel.getVertx().eventBus().consumer(maximName, this::handleMessage);
        this.consumer.exceptionHandler(throwable -> {
            getLogger().exception("CONSUMER EXCEPTION", throwable);
        });
    }

    private void handleMessage(Message<JsonObject> message) {
        getLogger().info("MESSAGE RECEIVED ON " + maximName);
        getLogger().info("MESSAGE RECEIVED ON " + maximName, message.body());
        MaximBullet maximBullet = new MaximBullet() {
            @Override
            public Future<Void> fire() {
                return Future.failedFuture(new NullPointerException());
            }
        };
        maximBullet.reloadDataFromJsonObject(message.body());
        product(maximBullet);
    }

    private Future<Void> routine() {
        return Keel.callFutureUntil(() -> getAsyncMap()
                .compose(map -> map.size().compose(size -> {
                    if (size > 0) {
                        getLogger().info("QUEUE SIZE IS " + size);
                        return consume().compose(v -> Future.succeededFuture(false));
                    } else {
                        getLogger().warning("QUEUE SIZE IS 0 NOW!");
                        return Future.succeededFuture(true);
                    }
                })));
    }
}
