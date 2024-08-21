package io.github.sinri.keel.core.cutter;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.19
 */
@TechnicalPreview(since = "3.0.19")
public class CutterOnString implements Cutter<String> {
    private final String cutterId;
    private String buffer;
    private int ptr = 0;

    private Handler<String> componentHandler;

    public CutterOnString() {
        this.cutterId = "Cutter::" + UUID.randomUUID();
        this.buffer = "";
    }

    @Override
    public Cutter<String> setComponentHandler(Handler<String> componentHandler) {
        this.componentHandler = componentHandler;
        return this;
    }

    /**
     * @since 3.2.15
     */
    @Override
    public final Future<Void> end() {
        if (!buffer.isEmpty()) {
            List<String> list = new ArrayList<>();
            if (buffer.contains("\n\n")) {
                String[] split = buffer.split("\n\n");
                Collections.addAll(list, split);
            } else {
                list.add(buffer);
            }
            return KeelAsyncKit.iterativelyCall(list, s -> {
                if (componentHandler != null && s != null) {
                    componentHandler.handle(s);
                }
                return Future.succeededFuture();
            });
        } else {
            return Future.succeededFuture();
        }
    }

    @Override
    public final void handle(Buffer piece) {
        KeelAsyncKit.exclusivelyCall(this.cutterId, (Supplier<Future<Void>>) () -> {
                    buffer += piece.toString(StandardCharsets.UTF_8);
                    return Future.succeededFuture();
                })
                .onSuccess(v -> {
                    cut();
                })
                .onFailure(throwable -> {
                    Keel.getLogger().exception(throwable, "Cutter::handle ERROR");
                });
    }

    /**
     * If there are double NewLine chars, cut from head to the DNL and send to componentHandler, and the left part left.
     */
    private Future<Void> cut() {
        AtomicReference<String> component = new AtomicReference<>();
        return KeelAsyncKit.exclusivelyCall(this.cutterId, (Supplier<Future<Void>>) () -> {
                    if (buffer.length() > ptr) {
                        var rest = buffer.substring(ptr);
                        int delimiterIndex = rest.indexOf("\n\n");
                        if (delimiterIndex >= 0) {
                            component.set(rest.substring(0, delimiterIndex));
                            ptr += delimiterIndex + 2;

                            buffer = buffer.substring(ptr);
                            ptr = 0;
                        }
                    }
                    return Future.succeededFuture();
                })
                .compose(done -> {
                    if (componentHandler != null && component.get() != null) {
                        componentHandler.handle(component.get());
                    }
                    return Future.succeededFuture();
                })
                .onFailure(throwable -> {
                    Keel.getLogger().exception(throwable, "Cutter::cut ERROR");
                })
                .compose(v -> {
                    return Future.succeededFuture();
                });
    }
}
