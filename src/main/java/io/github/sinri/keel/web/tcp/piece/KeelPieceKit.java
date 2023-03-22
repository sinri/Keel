package io.github.sinri.keel.web.tcp.piece;

import io.vertx.core.buffer.Buffer;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * 配合 KeelSisiodosi 食用。
 *
 * 
 * @since 2.8
 */
public abstract class KeelPieceKit<P extends KeelPiece> implements Consumer<Buffer> {
    private final Queue<P> pieceQueue;
    private Buffer buffer;

    public KeelPieceKit() {
        this.buffer = Buffer.buffer();
        this.pieceQueue = new ConcurrentLinkedQueue<>();
    }

    protected Buffer getBuffer() {
        return buffer;
    }

    protected void cutBuffer(int newStart) {
        buffer = buffer.getBuffer(newStart, buffer.length());
    }

    public Queue<P> getPieceQueue() {
        return pieceQueue;
    }

    /**
     * THREAD SAFE NEEDED
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
     * THREAD SAFE NEEDED.
     */
    abstract protected P parseFirstPieceFromBuffer();
}
