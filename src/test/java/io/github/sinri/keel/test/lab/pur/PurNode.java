package io.github.sinri.keel.test.lab.pur;

import io.github.sinri.keel.facade.cluster.pur.KeelPur;
import io.github.sinri.keel.facade.cluster.pur.KeelPurNodeInfo;
import io.github.sinri.keel.facade.cluster.pur.config.KeelPurConfig;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.ThreadingModel;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonArray;

import java.util.List;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class PurNode {
    public static void main(String[] args) {
        Keel.initializeVertxStandalone(new VertxOptions());
        KeelPur keelPur = new KeelPur(new KeelPurConfig()
                .setPorts(List.of(10400, 10405))
                .setAddresses(List.of("127.0.0.1"))
        );

        KeelEventLogger logger = KeelOutputEventLogCenter.getInstance().createLogger("PUR");
        keelPur.deployMe(new DeploymentOptions().setThreadingModel(ThreadingModel.WORKER))
                .onSuccess(deploymentId -> {
                    logger.notice("PUR DEPLOYED: " + deploymentId);
                })
                .onFailure(throwable -> {
                    logger.exception(throwable, "PUR DEPLOY FAILED");
                });

        Keel.getVertx().setPeriodic(5000L, timer -> {
            List<KeelPurNodeInfo> nodesInCluster = keelPur.getNodeManager().getRemoteNodesInCluster();

            JsonArray array = new JsonArray();
            nodesInCluster.forEach(nodeInfo -> {
//                String nodeId = nodeInfo.getNodeId();
                array.add(nodeInfo.getNodeId() + "(" + nodeInfo.getClientEndpoint() + ")");
            });
            logger.info(log -> log.message("remote nodes").put("nodes", array));
        });
    }
}
