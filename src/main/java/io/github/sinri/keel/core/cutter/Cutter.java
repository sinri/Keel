package io.github.sinri.keel.core.cutter;

import io.github.sinri.keel.core.TechnicalPreview;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;

/**
 * When receiving a stream, which could be cut and parsed into entities of type or class T.
 * Class Buffer of io.vertx has a bug when its length is too long.
 *
 * @param <T> The type of the cut entity as component of a stream holds.
 * @since 3.0.19
 */
@TechnicalPreview(since = "3.0.19")
public interface Cutter<T> {
    /**
     * Set the handler for the cut and parsed component.
     *
     * @param componentHandler the handler to handle the cut and parsed component from a stream.
     * @return this cutter
     */
    Cutter<T> setComponentHandler(Handler<T> componentHandler);

    /**
     * Declare the end of the stream.
     *
     * @return async end of the declaration.
     */
    Future<Void> end();

    /**
     * @param piece the stream recently incoming buffer.
     */
    void handle(Buffer piece);
}
