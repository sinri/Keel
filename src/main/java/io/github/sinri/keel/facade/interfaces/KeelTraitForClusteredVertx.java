package io.github.sinri.keel.facade.interfaces;

import com.hazelcast.config.*;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeInfo;
import io.vertx.spi.cluster.hazelcast.ConfigUtil;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;

import javax.annotation.Nullable;
import java.util.List;

public interface KeelTraitForClusteredVertx extends KeelTraitForVertx {

    static ClusterManager createClusterManagerForSAE(
            String clusterName,
            List<String> members,
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

    @Nullable
    ClusterManager getClusterManager();

    boolean isRunningInVertxCluster();

    /**
     * 构建一个简易集群。
     * 可以直接用于 SAE on Aliyun.
     *
     * @param clusterName  集群名称
     * @param members      集群组内地址成员
     * @param port         起始端口
     * @param portCount    递增尝试端口数量
     * @param vertxOptions Vert.x 参数
     * @return 未来
     * @since 2.9.1
     */
    default Future<Void> initializeVertx(
            String clusterName,
            List<String> members,
            int port, int portCount,
            VertxOptions vertxOptions
    ) {
        var clusterManager = KeelTraitForClusteredVertx.createClusterManagerForSAE(clusterName, members, port, portCount);
        vertxOptions.setClusterManager(clusterManager);
        return this.initializeVertx(vertxOptions);
    }

    default String getVertxNodeNetAddress() {
        if (getClusterManager() == null) return null;
        NodeInfo nodeInfo = getClusterManager().getNodeInfo();
        return nodeInfo.host() + ":" + nodeInfo.port();
    }

    default String getVertxNodeID() {
        if (getClusterManager() == null) return null;
        return getClusterManager().getNodeId();
    }

}
