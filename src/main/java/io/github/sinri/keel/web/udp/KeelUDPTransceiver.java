package io.github.sinri.keel.web.udp;

import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.net.SocketAddress;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * @since 2.8
 * Once named KeelUDPServer.
 */
public class KeelUDPTransceiver {
    private final int port;
    private final DatagramSocket udpServer;
    private KeelEventLogger logger;
    private BiConsumer<SocketAddress, Buffer> datagramSocketConsumer = (sender, buffer) -> {
        // do nothing
    };

    public KeelUDPTransceiver(DatagramSocket udpServer, int port, KeelEventLogger logger) {
        this.port = port;
        this.udpServer = udpServer;
        this.logger = logger;
    }

    public KeelEventLogger getLogger() {
        return logger;
    }

    public KeelUDPTransceiver setLogger(KeelEventLogger logger) {
        this.logger = logger;
        return this;
    }

    public KeelUDPTransceiver setDatagramSocketConsumer(BiConsumer<SocketAddress, Buffer> datagramSocketConsumer) {
        Objects.requireNonNull(datagramSocketConsumer);
        this.datagramSocketConsumer = datagramSocketConsumer;
        return this;
    }

    public Future<Object> listen() {
        return udpServer.listen(port, "0.0.0.0")
                .compose(datagramSocket -> {
                    datagramSocket.handler(datagramPacket -> {
                                SocketAddress sender = datagramPacket.sender();
                                Buffer data = datagramPacket.data();

                                logger.info("from " + sender.hostAddress() + ":" + sender.port() + " received: " + data);
                                this.datagramSocketConsumer.accept(sender, data);
                            })
                            .endHandler(end -> {
                                logger.info("read end");
                            })
                            .exceptionHandler(throwable -> {
                                logger.exception(throwable, "read error");
                            });
                    return Future.succeededFuture();
                });
    }

    public Future<Void> send(Buffer buffer, int targetPort, String targetAddress) {
        return udpServer.send(buffer, targetPort, targetAddress)
                .onSuccess(done -> {
                    logger.info("sent to " + targetAddress + ":" + targetPort + " data: " + buffer);
                })
                .onFailure(throwable -> {
                    logger.exception(throwable, "failed to send to " + targetAddress + ":" + targetPort);
                });
    }

    public Future<Void> close() {
        return udpServer.close()
                .onSuccess(v -> {
                    logger.info("closed");
                })
                .onFailure(throwable -> {
                    logger.exception(throwable, "failed to close");
                });
    }
}
