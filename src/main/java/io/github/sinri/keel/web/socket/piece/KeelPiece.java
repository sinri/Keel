package io.github.sinri.keel.web.socket.piece;

import io.vertx.core.buffer.Buffer;

/**
 * @since 2.8
 */
public interface KeelPiece {
    Buffer toBuffer();
}
