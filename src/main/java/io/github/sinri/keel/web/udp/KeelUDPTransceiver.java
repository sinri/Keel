package io.github.sinri.keel.web.udp;

import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.net.SocketAddress;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * @since 2.8
 * Once named KeelUDPServer.
 */
public class KeelUDPTransceiver {
    private final int port;
    private final DatagramSocket udpServer;
    /**
     * @since 3.2.0
     */
    private @Nonnull KeelIssueRecorder<DatagramIssueRecord> issueRecorder;
    private BiConsumer<SocketAddress, Buffer> datagramSocketConsumer = (sender, buffer) -> {
        // do nothing
    };

    public KeelUDPTransceiver(DatagramSocket udpServer, int port, @Nonnull KeelIssueRecorder<DatagramIssueRecord> issueRecorder) {
        this.port = port;
        this.udpServer = udpServer;
        this.setIssueRecorder(issueRecorder);
    }

    /**
     * @since 3.2.0
     */
    public @Nonnull KeelIssueRecorder<DatagramIssueRecord> getIssueRecorder() {
        return issueRecorder;
    }

    /**
     * @since 3.2.0
     */
    public KeelUDPTransceiver setIssueRecorder(KeelIssueRecorder<DatagramIssueRecord> issueRecorder) {
        this.issueRecorder = issueRecorder;
        this.issueRecorder.setRecordFormatter(r -> r.classification("port:" + port));
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

                                getIssueRecorder().info(r -> r
                                        .bufferReceived(data, sender.hostAddress(), sender.port())
                                );
                                this.datagramSocketConsumer.accept(sender, data);
                            })
                            .endHandler(end -> {
                                getIssueRecorder().info(r -> r.message("read end"));
                            })
                            .exceptionHandler(throwable -> {
                                getIssueRecorder().exception(throwable, r -> r.message("read error"));
                            });
                    return Future.succeededFuture();
                });
    }

    public Future<Void> send(Buffer buffer, int targetPort, String targetAddress) {
        return udpServer.send(buffer, targetPort, targetAddress)
                .onSuccess(done -> {
                    getIssueRecorder().info(r -> r.bufferSent(buffer, targetAddress, targetPort));
                })
                .onFailure(throwable -> {
                    getIssueRecorder().exception(throwable, r -> r.message("failed to send to " + targetAddress + ":" + targetPort));
                });
    }

    public Future<Void> close() {
        return udpServer.close()
                .onSuccess(v -> {
                    getIssueRecorder().info(r -> r.message("closed"));
                })
                .onFailure(throwable -> {
                    getIssueRecorder().exception(throwable, r -> r.message("failed to close"));
                });
    }
}
