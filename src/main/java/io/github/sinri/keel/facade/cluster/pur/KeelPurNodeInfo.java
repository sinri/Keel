package io.github.sinri.keel.facade.cluster.pur;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * @since 3.1.3
 */
@TechnicalPreview(since = "3.1.3")
public class KeelPurNodeInfo extends SimpleJsonifiableEntity {
    private long expireTimestamp;

//    /**
//     * The remote node's address and its port mapped to the local server socket
//     */
//    @Nullable
//    private String serverEndpoint;

    public KeelPurNodeInfo() {
        this.setNodeId(UUID.randomUUID().toString());
    }

    public KeelPurNodeInfo(JsonObject jsonObject) {
        super(jsonObject);
    }

    public String getNodeId() {
        return readString("node_id");
    }

    public KeelPurNodeInfo setNodeId(String nodeId) {
        this.jsonObject.put("node_id", nodeId);
        return this;
    }

    public KeelPurNodeInfo refreshExpireTimestamp() {
        this.expireTimestamp = System.currentTimeMillis() + 3000L;
        return this;
    }

    public boolean isExpired() {
        return this.expireTimestamp <= System.currentTimeMillis();
    }

    /**
     * @return The remote node's address and port
     */
    @Nullable
    public String getClientEndpoint() {
        return this.jsonObject.getString("client_endpoint");
    }

    /**
     * @param clientEndpoint The remote node's address and port
     */
    public void setClientEndpoint(@Nullable String clientEndpoint) {
        this.jsonObject.put("client_endpoint", clientEndpoint);
    }
//
//    public void updateServerEndpoint(@Nullable String serverEndpoint) {
//        this.serverEndpoint = serverEndpoint;
//    }
//
//    @Nullable
//    public String getServerEndpoint() {
//        return serverEndpoint;
//    }

}
