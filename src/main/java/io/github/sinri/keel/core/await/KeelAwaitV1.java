package io.github.sinri.keel.core.await;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.MessageConsumer;

import java.util.function.Function;

@Deprecated
public class KeelAwaitV1<T> {
    private static final DeliveryOptions deliveryOptions;

    static {
        AwaitStatusMessageCodec awaitStatusMessageCodec = new AwaitStatusMessageCodec();
        Keel.getEventBus().registerCodec(awaitStatusMessageCodec);
        deliveryOptions = new DeliveryOptions().setCodecName(awaitStatusMessageCodec.name());
    }

    private final String messageId;
    private final Function<Void, Future<T>> asyncFunction;
    private AwaitStatus awaitStatus;
    private T result;
    private Throwable error;

    public KeelAwaitV1(Function<Void, Future<T>> asyncFunction) {
        messageId = this.toString();

        MessageConsumer<AwaitStatus> objectMessageConsumer = Keel.getEventBus().consumer(messageId);
        objectMessageConsumer.handler(integerMessage -> {
            awaitStatus = integerMessage.body();
        });

        awaitStatus = AwaitStatus.INIT;
        result = null;
        error = null;

        this.asyncFunction = asyncFunction;
    }

    public T execute() throws Throwable {
        asyncFunction.apply(null)
                .onComplete(tAsyncResult -> {
                    if (tAsyncResult.succeeded()) {
                        result = tAsyncResult.result();
                        Keel.getEventBus().publish(messageId, AwaitStatus.DONE, deliveryOptions);
                    } else {
                        error = tAsyncResult.cause();
                        Keel.getEventBus().publish(messageId, AwaitStatus.ERROR, deliveryOptions);
                    }
                });

        while (true) {
            if (this.awaitStatus == AwaitStatus.INIT) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }
            if (this.awaitStatus == AwaitStatus.DONE) {
                return result;
            }
            if (this.awaitStatus == AwaitStatus.ERROR) {
                throw error;
            }
        }
    }

//    public static <T> T x(T a){
//
//    }

    private enum AwaitStatus {
        INIT, DONE, ERROR;

        public static AwaitStatus parse(String name) {
            switch (name) {
                case "INIT":
                    return INIT;
                case "DONE":
                    return DONE;
                case "ERROR":
                    return ERROR;
                default:
                    throw new RuntimeException("unknown name");
            }
        }
    }

    private static class AwaitStatusMessageCodec implements MessageCodec<AwaitStatus, AwaitStatus> {

        @Override
        public void encodeToWire(Buffer buffer, AwaitStatus awaitStatus) {
            buffer.appendString(awaitStatus.name());
        }

        @Override
        public AwaitStatus decodeFromWire(int i, Buffer buffer) {
            String string = buffer.getString(i, buffer.length());
            return AwaitStatus.parse(string);
        }

        @Override
        public AwaitStatus transform(AwaitStatus awaitStatus) {
            return awaitStatus;
        }

        @Override
        public String name() {
            return "AwaitStatusMessageCodec";
        }

        @Override
        public byte systemCodecID() {
            return -1;
        }
    }
}
