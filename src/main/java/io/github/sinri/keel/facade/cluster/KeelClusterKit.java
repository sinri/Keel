package io.github.sinri.keel.facade.cluster;

import com.hazelcast.config.*;
import io.github.sinri.keel.core.TechnicalPreview;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeInfo;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @since 3.0.0
 * This interface provides a shortcut to create an instance of ClusterManager for SAE.
 */
public interface KeelClusterKit {

    @TechnicalPreview(since = "3.1.0")
    @Nullable
    ClusterManager getClusterManager();

    static ClusterManager createClusterManagerForSAE(
            @NotNull String clusterName,
            @NotNull List<String> members,
            int port, int portCount
    ) {
        TcpIpConfig tcpIpConfig = new TcpIpConfig()
                .setEnabled(true)
                .setConnectionTimeoutSeconds(1);
        members.forEach(tcpIpConfig::addMember);

        JoinConfig joinConfig = new JoinConfig()
                .setMulticastConfig(new MulticastConfig().setEnabled(false))
                .setTcpIpConfig(tcpIpConfig);

        NetworkConfig networkConfig = new NetworkConfig()
                .setJoin(joinConfig)
                .setPort(port)
                .setPortCount(portCount)
                .setPortAutoIncrement(portCount > 1)
                .setOutboundPorts(List.of(0));

        Config hazelcastConfig = ConfigUtil.loadConfig()
                .setClusterName(clusterName)
                .setNetworkConfig(networkConfig);

        return new HazelcastClusterManager(hazelcastConfig);
    }

    @TechnicalPreview(since = "3.1.0")
    default @NotNull String getVertxNodeNetAddress() {
        if (getClusterManager() == null) return "";
        NodeInfo nodeInfo = getClusterManager().getNodeInfo();
        return nodeInfo.host() + ":" + nodeInfo.port();
    }

    @TechnicalPreview(since = "3.1.0")
    default @NotNull String getVertxNodeID() {
        if (getClusterManager() == null) return "";
        return getClusterManager().getNodeId();
    }
}
