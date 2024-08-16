package io.github.sinri.keel.web.udp;

import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 3.2.0
 */
public final class DatagramIssueRecord extends BaseIssueRecord<DatagramIssueRecord> {
    public static final String TopicUdpDatagram = "UdpDatagram";


    @Override
    public @NotNull String topic() {
        return TopicUdpDatagram;
    }

    @Override
    public @NotNull DatagramIssueRecord getImplementation() {
        return this;
    }

    private DatagramIssueRecord buffer(@NotNull Buffer buffer, @NotNull String address, int port, @NotNull String action) {
        this.context(action, new JsonObject()
                        .put("address", action)
                        .put("port", port)
                )
                .context("buffer", new JsonObject()
                        .put("buffer_content", KeelHelpers.binaryHelper().encodeHexWithUpperDigits(buffer))
                        .put("buffer_size", buffer.length())
                );
        return this;
    }

    public DatagramIssueRecord bufferSent(@NotNull Buffer buffer, @NotNull String address, int port) {
        return this.buffer(buffer, address, port, "sent_to");
    }

    public DatagramIssueRecord bufferReceived(@NotNull Buffer buffer, @NotNull String address, int port) {
        return this.buffer(buffer, address, port, "received_from");
    }
}
