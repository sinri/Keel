package io.github.sinri.keel.web.service;

import io.github.sinri.keel.Keel;
import io.vertx.core.shareddata.Counter;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.PlatformHandler;

import java.util.Random;
import java.util.UUID;

/**
 * @since 2.9.2
 */
public class KeelPlatformHandler implements PlatformHandler {
    public final static String KEEL_REQUEST_ID = "KEEL_REQUEST_ID"; // -> String
    public final static String KEEL_REQUEST_START_TIME = "KEEL_REQUEST_START_TIME"; // -> long * 0.001 second
    public final static String KEEL_REQUEST_CLIENT_IP_CHAIN = "KEEL_REQUEST_CLIENT_IP_CHAIN"; // -> List<String of IP>

    @Override
    public void handle(RoutingContext routingContext) {
        // START !
        Keel.getVertx().sharedData()
                .getCounter("KeelPlatformHandler-RequestID-Counter")
                .compose(Counter::incrementAndGet)
                .andThen(id_ar -> {
                    long id;
                    if (id_ar.failed()) {
                        id = new Random().nextLong() * -1;
                    } else {
                        id = id_ar.result();
                    }
                    routingContext.put(KEEL_REQUEST_ID, Keel.helpers().net().getLocalHostAddress() + "[" + id + "]" + UUID.randomUUID());

                    routingContext.put(KEEL_REQUEST_START_TIME, System.currentTimeMillis());
                    routingContext.put(KEEL_REQUEST_CLIENT_IP_CHAIN, Keel.helpers().net().parseWebClientIPChain(routingContext));

                })
                .andThen(v -> {
                    // NEXT !
                    routingContext.next();
                });
    }
}
