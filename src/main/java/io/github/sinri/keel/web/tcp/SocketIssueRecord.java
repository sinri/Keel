package io.github.sinri.keel.web.tcp;

import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 3.2.0
 */
public final class SocketIssueRecord extends BaseIssueRecord<SocketIssueRecord> {
    public static final String TopicTcpSocket = "TcpSocket";

    @Override
    public @NotNull SocketIssueRecord getImplementation() {
        return this;
    }

    public SocketIssueRecord buffer(@NotNull Buffer buffer) {
        this.context("buffer", new JsonObject()
                .put("buffer_content", KeelHelpers.binaryHelper().encodeHexWithUpperDigits(buffer))
                .put("buffer_size", buffer.length())
        );
        return this;
    }

    @Override
    public @NotNull String topic() {
        return TopicTcpSocket;
    }
}
