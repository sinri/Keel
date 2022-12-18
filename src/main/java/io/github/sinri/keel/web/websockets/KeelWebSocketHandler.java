package io.github.sinri.keel.web.websockets;

import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.AbstractVerticle;

abstract public class KeelWebSocketHandler extends AbstractVerticle implements KeelVerticle {
    // todo handler 的逻辑似乎有问题
//    private final ServerWebSocket webSocket;
//
//    public KeelWebSocketHandler(ServerWebSocket webSocket) {
//        super();
//        this.webSocket = webSocket;
//    }
//
//    /**
//     * @since 2.4
//     */
//    public static <T extends KeelWebSocketHandler> void handle(
//            Keel keel,
//            ServerWebSocket webSocket,
//            Class<T> handlerClass,
//            KeelEventLogger logger,
//            DeploymentOptions deploymentOptions
//    ) {
//        T keelWebSocketHandler;
//        try {
//            keelWebSocketHandler = handlerClass.getConstructor(ServerWebSocket.class).newInstance(webSocket);
//        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
//                 NoSuchMethodException e) {
//            e.printStackTrace();
//            webSocket.reject();
//            return;
//        }
//        keelWebSocketHandler.deployMe(keel, deploymentOptions)
//                .compose(deploymentID -> {
//                    logger.debug("deploymentID as " + deploymentID);
//                    return Future.succeededFuture();
//                })
//                .recover(throwable -> {
//                    logger.exception(throwable);
//                    return Future.succeededFuture();
//                });
//    }
//
//    public static void upgradeFromHttp(
//            Keel keel,
//            Route route,
//            Class<? extends KeelWebSocketHandler> handlerClass,
//            KeelEventLogger logger,
//            DeploymentOptions deploymentOptions
//    ) {
//        route.handler(routingContext -> {
//            logger.debug(routingContext.request().toString());
//            routingContext.request().toWebSocket(serverWebSocketAsyncResult -> {
//                if (serverWebSocketAsyncResult.failed()) {
//                    logger.exception(serverWebSocketAsyncResult.cause(), "Failed to get webSocketFromHttp");
//                } else {
//                    ServerWebSocket webSocketFromHttp = serverWebSocketAsyncResult.result();
//                    logger.debug("Got webSocketFromHttp " + webSocketFromHttp.toString());
//                    handle(keel, webSocketFromHttp, handlerClass, logger, deploymentOptions);
//                }
//            });
//        });
//    }
//
//    /**
//     * @since 2.4
//     */
//    protected SocketAddress getWebSocketRemoteAddress() {
//        return this.webSocket.remoteAddress();
//    }
//
//    /**
//     * @since 2.4
//     */
//    protected SocketAddress getWebSocketLocalAddress() {
//        return this.webSocket.localAddress();
//    }
//
//    /**
//     * @since 2.4
//     */
//    protected String getWebSocketPath() {
//        return this.webSocket.path();
//    }
//
//    /**
//     * @since 2.4
//     */
//    protected String getWebSocketHost() {
//        return this.webSocket.host();
//    }
//
//    /**
//     * @since 2.4
//     */
//    protected String getWebSocketUri() {
//        return this.webSocket.uri();
//    }
//
//    /**
//     * @since 2.4
//     */
//    protected String getWebSocketQuery() {
//        return this.webSocket.query();
//    }
//
//    /**
//     * @since 2.4
//     */
//    protected String getWebSocketScheme() {
//        return this.webSocket.scheme();
//    }
//
//    abstract protected KeelEventLogger prepareLogger();
//
//    private KeelEventLogger logger;
//
//    @Override
//    public KeelEventLogger getEventLogger() {
//        return logger;
//    }
//
//    @Override
//    public void setEventLogger(KeelEventLogger logger) {
//        this.logger = logger;
//    }
//
//    @Override
//    public void start() throws Exception {
//        super.start();
//        setLogger(prepareLogger());
//
//        webSocket
//                .handler(this::handleBuffer)
//                .exceptionHandler(this::handleException)
//                .endHandler(end -> {
//                    this.handleEnd();
//                    this.undeploy();
//                });
//
//        shouldReject()
//                .compose(shouldReject -> {
//                    if (shouldReject) {
//                        webSocket.reject();
//                        return this.undeploy();
//                    } else {
//                        accept();
//                    }
//                    return Future.succeededFuture();
//                });
//    }
//
//    protected Future<Boolean> shouldReject() {
//        return Future.succeededFuture(false);
//    }
//
//    abstract protected void accept();
//
//    abstract protected void handleBuffer(Buffer buffer);
//
//    abstract protected void handleException(Throwable throwable);
//
//    /**
//     * @since 2.7 无需额外调用 undeploy，由类的基本实现保证。
//     */
//    abstract protected void handleEnd();
//
//    protected final Future<Void> undeploy() {
//        String deploymentID = deploymentID();
//        return this.undeployMe().compose(v -> {
////            Keel.unregisterDeployedKeelVerticle(deploymentID);
//            return Future.succeededFuture();
//        });
//    }
//
//    protected final Future<Void> writeText(String text) {
//        return webSocket.writeTextMessage(text);
//    }
//
//    /**
//     * @since 2.4
//     */
//    protected final Future<Void> writeBuffer(Buffer buffer) {
//        return webSocket.write(buffer);
//    }
//
//    /**
//     * @since 2.7 关闭后解除部署
//     */
//    protected final Future<Void> close() {
//        return webSocket.close()
//                .compose(v -> this.undeploy());
//    }
}
