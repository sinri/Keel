package io.github.sinri.keel.facade.cluster.pur;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.vertx.core.json.JsonArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 3.1.3
 */
@TechnicalPreview(since = "3.1.3")
public class KeelPurNodeManager {
    private final KeelPurNodeInfo localNodeInfo;
    private final Map<String, KeelPurNodeInfo> remoteNodeMap;

    private final KeelEventLogger logger;

    public KeelPurNodeManager() {
        this.localNodeInfo = new KeelPurNodeInfo();
        this.remoteNodeMap = new ConcurrentHashMap<>();

        this.logger = KeelOutputEventLogCenter.getInstance().createLogger("PurNodeManager");
    }

    public synchronized void confirmedNodeAlive(@Nonnull KeelPurNodeInfo nodeInfo) {
        nodeInfo.refreshExpireTimestamp();
        this.remoteNodeMap.put(nodeInfo.getNodeId(), nodeInfo);

//        this.logger.info(log -> log.message("confirmedNodeAlive").put("node_info", nodeInfo.toJsonObject()));
    }

//    public synchronized void confirmedNodeDeadWithClientEndpoint(@Nullable String clientEndpoint) {
//        JsonArray array = new JsonArray();
//        this.remoteNodeMap.forEach((k, v) -> {
//            if (Objects.equals(v.getClientEndpoint(), clientEndpoint)) {
//                KeelPurNodeInfo removed = this.remoteNodeMap.remove(k);
//                array.add(removed.getNodeId());
//            }
//        });
//
//        this.logger.info(log -> log.message("confirmedNodeDeadWithClientEndpoint").put("removed", array));
//    }

    public synchronized void confirmedNodeDeadWithNodeId(@Nullable String nodeId) {
        JsonArray array = new JsonArray();
        this.remoteNodeMap.forEach((k, v) -> {
            if (Objects.equals(v.getNodeId(), nodeId)) {
                KeelPurNodeInfo removed = this.remoteNodeMap.remove(k);
                array.add(removed.getNodeId());
            }
        });

//        this.logger.info(log -> log.message("confirmedNodeDeadWithNodeId").put("removed", array));
    }

    public synchronized Set<KeelPurNodeInfo> seekWithClientEndpoint(String clientEndpoint) {
        Set<KeelPurNodeInfo> set = new HashSet<>();
        this.remoteNodeMap.forEach((k, v) -> {
            if (Objects.equals(v.getClientEndpoint(), clientEndpoint)) {
                set.add(v);
            }
        });
        return set;
    }

    public synchronized Set<String> seekNodeIdsWithClientEndpoint(String clientEndpoint) {
        Set<String> set = new HashSet<>();
        this.remoteNodeMap.forEach((k, v) -> {
            if (Objects.equals(v.getClientEndpoint(), clientEndpoint)) {
                set.add(v.getNodeId());
            }
        });
        return set;
    }

    public synchronized void close() {
        this.remoteNodeMap.clear();

//        logger.info("close");
    }


    public KeelPurNodeInfo getLocalNodeInfo() {
        return localNodeInfo;
    }

    public synchronized List<KeelPurNodeInfo> getRemoteNodesInCluster() {
        this.remoteNodeMap.forEach((k, v) -> {
            if (v.isExpired()) {
                this.remoteNodeMap.remove(k);
            }
        });
        return List.copyOf(remoteNodeMap.values());
    }

}
