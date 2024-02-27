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
    public static final String AttributeRespondInfo = "RespondInfo";

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
            @Nonnull Class<?> receptionistClass,
            @Nullable String query,
            @Nullable String body
    ) {
        var x = new JsonObject()
                .put("method", method.name())
                .put("path", path)
                .put("handler", receptionistClass.getName());
        if (query != null) x.put("query", query);
        if (body != null) x.put("body", body);
        this.attribute(AttributeRequest, x);
        return this;
    }

    public ReceptionistIssueRecord setResponse(@Nullable Object responseBody) {
        this.attribute(AttributeResponse, new JsonObject()
                .put("body", responseBody)
        );
        return this;
    }

    public ReceptionistIssueRecord setRespondInfo(
            int statusCode,
            @Nullable String statusMessage,
            boolean ended,
            boolean closed
    ) {
        this.attribute(AttributeRespondInfo, new JsonObject()
                .put("code", statusCode)
                .put("message", statusMessage)
                .put("ended", ended)
                .put("closed", closed)
        );
        return this;
    }
}
