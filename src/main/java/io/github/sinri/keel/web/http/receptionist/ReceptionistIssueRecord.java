package io.github.sinri.keel.web.http.receptionist;

import io.github.sinri.keel.logger.issue.record.BaseIssueRecord;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @since 3.2.0
 */
public final class ReceptionistIssueRecord extends BaseIssueRecord<ReceptionistIssueRecord> {
    public static final String TopicReceptionist = "Receptionist";
    public static final String AttributeRequest = "request";
    public static final String AttributeResponse = "response";

    public ReceptionistIssueRecord(@Nonnull String requestId) {
        this.attribute("request_id", requestId);
    }

    @Nonnull
    @Override
    public ReceptionistIssueRecord getImplementation() {
        return this;
    }

    @Nonnull
    @Override
    public String topic() {
        return TopicReceptionist;
    }

    public ReceptionistIssueRecord setRequest(
            @Nonnull HttpMethod method,
            @Nonnull String path,
            @Nonnull Class<?> receptionistClass
    ) {
        this.attribute(AttributeRequest, new JsonObject()
                .put("method", method.name())
                .put("path", path)
                .put("handler", receptionistClass.getName())
        );
        return this;
    }

    public ReceptionistIssueRecord setResponse(
            int statusCode,
            @Nullable String statusMessage,
            boolean ended,
            boolean closed
    ) {
        this.attribute(AttributeResponse, new JsonObject()
                .put("code", statusCode)
                .put("message", statusMessage)
                .put("ended", ended)
                .put("closed", closed)
        );
        return this;
    }
}
