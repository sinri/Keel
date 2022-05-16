package io.github.sinri.keel.web.websockets;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Route;

import java.lang.reflect.InvocationTargetException;

abstract public class KeelWebSocketHandler extends KeelVerticle {
    private final ServerWebSocket webSocket;

    public KeelWebSocketHandler(ServerWebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public static void handle(ServerWebSocket webSocket, Class<? extends KeelWebSocketHandler> handlerClass) {
        KeelLogger logger = Keel.outputLogger("KeelWebSocketHandler::handle for " + handlerClass.getName());
        Keel.getVertx().getOrCreateContext();
        KeelWebSocketHandler keelWebSocketHandler;
        try {
            keelWebSocketHandler = handlerClass.getConstructor(ServerWebSocket.class).newInstance(webSocket);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            webSocket.reject();
            return;
        }
        keelWebSocketHandler.deployMe()
                .compose(deploymentID -> {
                    logger.debug("deploymentID as " + deploymentID);
                    return Future.succeededFuture();
                })
                .recover(throwable -> {
                    logger.exception(throwable);
                    return Future.succeededFuture();
                });

    }

    public static void upgradeFromHttp(Route route, Class<? extends KeelWebSocketHandler> handlerClass) {
        KeelLogger logger = Keel.outputLogger("KeelWebSocketHandler::upgradeFromHttp");
        route.handler(routingContext -> {
            logger.debug(routingContext.request().toString());
            routingContext.request().toWebSocket(serverWebSocketAsyncResult -> {
                if (serverWebSocketAsyncResult.failed()) {
                    logger.exception("Failed to get webSocketFromHttp", serverWebSocketAsyncResult.cause());
                } else {
                    ServerWebSocket webSocketFromHttp = serverWebSocketAsyncResult.result();
                    logger.debug("Got webSocketFromHttp " + webSocketFromHttp.toString());
                    handle(webSocketFromHttp, handlerClass);
                }
            });
        });
    }

    protected ServerWebSocket getWebSocket() {
        return webSocket;
    }

    abstract protected KeelLogger prepareLogger();

    @Override
    public final void start() throws Exception {
        super.start();
        Keel.registerDeployedKeelVerticle(this);
        KeelLogger logger = prepareLogger();
        setLogger(logger);

        logger.info("START");
        if (getWebSocket() == null) {
            logger.error("null");
        }

        getLogger().info("incoming websocket " + getWebSocket().toString());

        getWebSocket()
                .handler(buffer -> {
                    context.runOnContext(inContext -> {
                        getLogger().info("HANDLE BUFFER " + buffer.toString());
                        handleBuffer(buffer);
                    });
                })
                .exceptionHandler(throwable -> {
                    context.runOnContext(v -> {
                        handleException(throwable);
                    });
                })
                .endHandler(end -> {
                    context.runOnContext(v -> {
                        handleEnding().eventually(vv -> undeployMe());
                    });
                });
        handleSocket();

    }

    protected final void handleSocket() {
        shouldAccept()
                .compose(shouldAccept -> {
                    getLogger().info("deployment: " + deploymentID());
                    if (!shouldAccept) {
                        getLogger().warning("to reject it");
                        getWebSocket().reject();
                        return Future.succeededFuture();
                    }
                    // The ServerWebSocket instance enables you to retrieve the headers, path, query and URI of the HTTP request of the WebSocket handshake.

                    getWebSocket().accept();

                    return sendTextMessageAfterAccept()
                            .compose(getWebSocket()::writeTextMessage);
                });
    }

    /**
     * Check if the websocket should be rejected
     *
     * @return void
     */
    protected Future<Boolean> shouldAccept() {
//        System.out.println("KeelWebSocketHandler::shouldAccept logger is " + getLogger().getUniqueLoggerID());
        getLogger().info("shouldAccept");
        return Future.succeededFuture(true);
    }

    abstract protected Future<String> sendTextMessageAfterAccept();

    abstract protected void handleBuffer(Buffer buffer);

    protected void handleException(Throwable throwable) {
        getLogger().exception("EXCEPTION", throwable);
        getWebSocket().end();
    }

    protected Future<Void> handleEnding() {
        getLogger().info("END");
        return Future.succeededFuture();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Keel.unregisterDeployedKeelVerticle(this.deploymentID());
    }
}
