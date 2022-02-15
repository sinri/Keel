package io.github.sinri.keel.core.await;

import io.github.sinri.keel.Keel;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.MessageConsumer;

import java.util.function.Function;

/**
 * This class is a trick to realize the `await` using `Thread.sleep` and EventBus.
 *
 * @param <T> the class of the object to wait for
 * @since 1.9
 */
public class KeelAwait<T> {
    private static final DeliveryOptions deliveryOptions;

    static {
        AwaitStatusMessageCodec awaitStatusMessageCodec = new AwaitStatusMessageCodec();
        Keel.getEventBus().registerCodec(awaitStatusMessageCodec);
        deliveryOptions = new DeliveryOptions().setCodecName(awaitStatusMessageCodec.name());
    }

    private final String messageId;
    private final MessageConsumer<AwaitStatus> objectMessageConsumer;
    private AwaitStatus awaitStatus;
    private T result;
    private Throwable error;
    private long waitTime = 100L;

    private KeelAwait() {
        messageId = this.toString();

        objectMessageConsumer = Keel.getEventBus().consumer(messageId, integerMessage -> awaitStatus = integerMessage.body());

        awaitStatus = AwaitStatus.INIT;
        result = null;
        error = null;
    }

    public static <K> K asyncExecute(Function<Void, Future<K>> asyncFunction) throws Throwable {
        return asyncExecute(asyncFunction, 100L);
    }

    public static <K> K asyncExecute(Function<Void, Future<K>> asyncFunction, long waitTime) throws Throwable {
        KeelAwait<K> await = new KeelAwait<>();
        await.setWaitTime(waitTime);
        K result = await.execute(asyncFunction);

        await.close();
        return result;
    }

    protected void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    private T execute(Function<Void, Future<T>> asyncFunction) throws Throwable {
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
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                    Keel.outputLogger("KeelAwait").exception(e);
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

    private void close() {
        objectMessageConsumer.unregister();
    }

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
