package io.github.sinri.Keel.servant;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonObject;

import java.util.function.Function;

public class KeelServant {
    private final EventBus eventBus;
    private final MessageConsumer<Object> consumer;

    public KeelServant(Vertx vertx, String channelName) {
        this.eventBus = vertx.eventBus();
        this.consumer = eventBus.consumer(channelName);
    }

    public KeelServant handleJsonObject(Function<JsonObject, String> jsonMessageHandleFunc) {
        consumer.handler(objectMessage -> {
//            MultiMap headers = objectMessage.headers();
            JsonObject body = (JsonObject) objectMessage.body();
            String result = jsonMessageHandleFunc.apply(body);
            objectMessage.reply(result);
        });
        return this;
    }

    public KeelServant handleString(Function<String, String> stringMessageHandleFunc) {
        consumer.handler(objectMessage -> {
//            MultiMap headers = objectMessage.headers();
            String body = (String) objectMessage.body();
            String result = stringMessageHandleFunc.apply(body);
            objectMessage.reply(result);
        });
        return this;
    }

    public void unregister() {
        consumer.unregister();
    }
}
