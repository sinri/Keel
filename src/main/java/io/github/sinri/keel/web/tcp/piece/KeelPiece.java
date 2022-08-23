package io.github.sinri.keel.web.tcp.piece;

import io.vertx.core.buffer.Buffer;

/**
 * @since 2.8
 */
public interface KeelPiece {
    Buffer toBuffer();
}
