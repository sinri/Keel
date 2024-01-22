package io.github.sinri.keel.facade.cluster.pur.mixin;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.facade.cluster.pur.KeelPurNodeInfo;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.1.3
 */
@TechnicalPreview(since = "3.1.3")
public interface KeelPurClientMixin extends KeelPurBaseMixin {
    @Nullable
    WebClient getClient();

    default Future<Void> startClients() {
        getLogger().info("start clients later");
        Keel.getVertx().setTimer(10L, timer -> {
            KeelAsyncKit.repeatedlyCall(routineResult -> {
                if (shouldStopClient()) {
                    routineResult.stop();
                    getLogger().warning("client is to stop");
                    return Future.succeededFuture();
                } else {
                    getLogger().info("client to ping");
                    return ping()
                            .onFailure(throwable -> {
                                getLogger().exception(throwable, "client ping error");
                            })
                            .eventually(() -> {
                                getLogger().info("client to rest after ping");
                                return KeelAsyncKit.sleep(clientRestTime());
                            });
                }
            });
        });
        return Future.succeededFuture();
    }

    boolean shouldStopClient();

    default long clientRestTime() {
        return 1000L;
    }

//    private Future<Void> join() {
//        return broadcast("/join", bufferHttpResponse -> {
//return Future.succeededFuture();
//        });
//    }

//    default Future<Void> leave() {
//        return broadcast("/leave", bufferHttpResponse -> {
//            return Future.succeededFuture();
//        });
//    }

    private Future<Void> ping() {
        return broadcast("/ping", bufferHttpResponse -> {
            return Future.succeededFuture();
        });
    }

    private Future<Void> broadcast(String url, Function<HttpResponse<Buffer>, Future<Void>> func) {
        return KeelAsyncKit.parallelForAllComplete(this.getConfig().getAddresses(), address -> {
            return KeelAsyncKit.parallelForAllComplete(this.getConfig().getPorts(), port -> {
                var clientEndpoint = address + ":" + port;
                getLogger().info("broadcasting, endpointOfTarget: " + clientEndpoint);
                WebClient client = getClient();
                Objects.requireNonNull(client);

                Set<String> clientEndpointMappedNodeIdSet = getNodeManager().seekNodeIdsWithClientEndpoint(clientEndpoint);
                if (clientEndpointMappedNodeIdSet.contains(this.getLocalNodeInfo().getNodeId())) {
                    return Future.succeededFuture();
                }

                return client.postAbs("http://" + clientEndpoint + url)
                        .sendJson(new JsonObject()
                                .put("node_info", this.getLocalNodeInfo().toJsonObject())
                                .put("target_node_endpoint", clientEndpoint)
                        )
                        .compose(bufferHttpResponse -> {
                            JsonObject nodeInfoData;
                            try {
                                JsonObject jsonObject = bufferHttpResponse.bodyAsJsonObject();
                                Objects.requireNonNull(jsonObject);

                                String code = jsonObject.getString("code");
                                if (!"OK".equals(code)) {
                                    return Future.failedFuture("code not OK");
                                }

                                nodeInfoData = jsonObject.getJsonObject("node_info");
                                Objects.requireNonNull(nodeInfoData);

                                KeelPurNodeInfo nodeInfo = new KeelPurNodeInfo(nodeInfoData);
                                getNodeManager().confirmedNodeAlive(nodeInfo);

                                return func.apply(bufferHttpResponse);
                            } catch (Throwable throwable) {
                                return Future.failedFuture(throwable);
                            }
                        })
                        .compose(v -> {
                            return Future.succeededFuture();
                        }, throwable -> {
                            // died
                            clientEndpointMappedNodeIdSet.forEach(nodeId -> {
                                getNodeManager().confirmedNodeDeadWithNodeId(nodeId);
                            });
                            return Future.succeededFuture();
                        });
            });
        });
    }
}
