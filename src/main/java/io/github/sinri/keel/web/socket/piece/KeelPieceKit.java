package io.github.sinri.keel.web.socket.piece;

import io.vertx.core.buffer.Buffer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * 配合 KeelSisiodosi 食用。
 *
 * @param <P>
 * @since 2.8
 */
public abstract class KeelPieceKit<P extends KeelPiece> implements Consumer<Buffer> {
    private final Buffer buffer;
    private final Queue<P> pieceQueue;

    public KeelPieceKit() {
        this.buffer = Buffer.buffer();
        this.pieceQueue = new ConcurrentLinkedQueue<>();
    }

    public Queue<P> getPieceQueue() {
        return pieceQueue;
    }

    /**
     * @implNote THREAD SAFE NEEDED
     */
    @Override
    public void accept(Buffer incomingBuffer) {
        if (incomingBuffer != null && incomingBuffer.length() > 0) {
            buffer.appendBuffer(incomingBuffer);

            while (true) {
                P piece = this.parseFirstPieceFromBuffer();
                if (piece == null) break;
                this.pieceQueue.offer(piece);
            }
        }
    }


    /**
     * Try to read first piece from buffer,
     * if piece found, save the rest buffer, return the piece;
     * or return null.
     *
     * @return first piece or null.
     * @implNote THREAD SAFE NEEDED.
     */
    abstract protected P parseFirstPieceFromBuffer();
}
