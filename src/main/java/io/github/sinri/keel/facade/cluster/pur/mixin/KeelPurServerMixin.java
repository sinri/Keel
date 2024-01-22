package io.github.sinri.keel.facade.cluster.pur.mixin;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.facade.cluster.pur.KeelPurNodeInfo;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.1.3
 */
@TechnicalPreview(since = "3.1.3")
public interface KeelPurServerMixin extends KeelPurBaseMixin {


    private static KeelPurNodeInfo parseNodeInfo(RoutingContext routingContext) {
        JsonObject jsonObject = routingContext.body().asJsonObject();
        return new KeelPurNodeInfo(jsonObject.getJsonObject("node_info"));
    }

    private static String parseFacadeEndpoint(RoutingContext routingContext) {
        JsonObject jsonObject = routingContext.body().asJsonObject();
        return jsonObject.getString("target_node_endpoint");
    }

    @Deprecated
    private static String parseServerEndpoint(RoutingContext routingContext) {
        return routingContext.request().remoteAddress().host() + ":" + routingContext.request().remoteAddress().port();
    }

    default Future<HttpServer> startServer() {
        var ports = this.getConfig().getPorts();
        var address = this.getConfig().getListenHost();
        AtomicInteger iRef = new AtomicInteger(0);
        AtomicReference<HttpServer> httpServerAtomicReference = new AtomicReference<>();
        AtomicInteger portRef = new AtomicInteger();
        return KeelAsyncKit.repeatedlyCall(routineResult -> {
                    int port = ports.get(iRef.get());
                    Router router = Router.router(Keel.getVertx());
                    configureRoutes(router);
                    getLogger().info("Server init trying port " + port);
                    return Keel.getVertx().createHttpServer(new HttpServerOptions()
                                    .setSsl(false)
                            )
                            .requestHandler(router)
                            .listen(port, address)
                            .compose(httpServer -> {
                                httpServerAtomicReference.set(httpServer);
                                routineResult.stop();
                                portRef.set(port);
                                this.getNodeManager().getLocalNodeInfo().setClientEndpoint(address + ":" + port);
                                return Future.succeededFuture();
                            }, throwable -> {
                                iRef.incrementAndGet();
                                if (iRef.get() >= ports.size()) {
                                    routineResult.stop();
                                }
                                return Future.succeededFuture();
                            });
                })
                .compose(v -> {
                    HttpServer httpServer = httpServerAtomicReference.get();

                    if (httpServer == null) {
                        return Future.failedFuture("Server set up failed");
                    }

                    getLogger().info("Server set up, port: " + portRef.get());

                    return Future.succeededFuture(httpServer);
                });
    }

    private void configureRoutes(Router router) {

        // when remote node joins cluster, it should broadcast JOIN.
//        router.route(HttpMethod.POST, "/join").handler(ctx -> {
//            var nodeInfo = parseNodeInfo(ctx);
//            String endpoint = parseEndpoint(ctx);
//            this.getNodeManager().confirmedNodeAlive(endpoint, nodeInfo);
//            respond(ctx);
//        });
        // when remote node leaves cluster, it should broadcast LEAVE.
//        router.route(HttpMethod.POST, "/leave").handler(ctx -> {
//            var nodeInfo = parseNodeInfo(ctx);
//            String endpoint = parseEndpoint(ctx);
//            this.getNodeManager().confirmedNodeDeadWithNodeId(nodeInfo.getNodeId());
//            this.getNodeManager().confirmedNodeDeadWithEndpoint(endpoint);
//            respond(ctx);
//        });
        // when remote node broadcast ping , it should pong back.
        router.route(HttpMethod.POST, "/ping")
                .handler(BodyHandler.create())
                .handler(ctx -> {
                    var nodeInfo = parseNodeInfo(ctx);

                    String localClientEndpoint = parseFacadeEndpoint(ctx);
                    this.getNodeManager().getLocalNodeInfo().setClientEndpoint(localClientEndpoint);

//                    String serverEndpoint = parseServerEndpoint(ctx);
                    this.getNodeManager().confirmedNodeAlive(nodeInfo);

                    respond(ctx);
                });
    }

    private void respond(RoutingContext routingContext) {
        routingContext.response().end(new JsonObject()
                .put("code", "OK")
                .put("node_info", this.getLocalNodeInfo().toJsonObject())
                .toBuffer()
        );
    }

    default Future<Void> stopServer(HttpServer server) {
        if (server != null) {
            return server.close();
        } else {
            return Future.succeededFuture();
        }
    }

}
