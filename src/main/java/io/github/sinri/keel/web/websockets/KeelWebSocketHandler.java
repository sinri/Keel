package io.github.sinri.keel.web.websockets;

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
        super();
        this.webSocket = webSocket;
    }

    public static <T extends KeelWebSocketHandler> void handle(ServerWebSocket webSocket, Class<T> handlerClass, KeelLogger logger) {
//        KeelLogger logger = Keel.outputLogger("KeelWebSocketHandler::handle for " + handlerClass.getName());
        T keelWebSocketHandler;
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

    public static void upgradeFromHttp(Route route, Class<? extends KeelWebSocketHandler> handlerClass, KeelLogger logger) {
//        KeelLogger logger = Keel.outputLogger("KeelWebSocketHandler::upgradeFromHttp");
        route.handler(routingContext -> {
            logger.debug(routingContext.request().toString());
            routingContext.request().toWebSocket(serverWebSocketAsyncResult -> {
                if (serverWebSocketAsyncResult.failed()) {
                    logger.exception("Failed to get webSocketFromHttp", serverWebSocketAsyncResult.cause());
                } else {
                    ServerWebSocket webSocketFromHttp = serverWebSocketAsyncResult.result();
                    logger.debug("Got webSocketFromHttp " + webSocketFromHttp.toString());
                    handle(webSocketFromHttp, handlerClass, logger);
                }
            });
        });
    }

    abstract protected KeelLogger prepareLogger();

    @Override
    public void start() throws Exception {
        super.start();
        setLogger(prepareLogger());

        webSocket
                .handler(this::handleBuffer)
                .exceptionHandler(this::handleException)
                .endHandler(end -> this.handleEnd());

        shouldReject()
                .compose(shouldReject -> {
                    if (shouldReject) {
                        webSocket.reject();
                    } else {
                        accept();
                    }
                    return Future.succeededFuture();
                });
    }

    protected Future<Boolean> shouldReject() {
        return Future.succeededFuture(false);
    }

//    protected boolean shouldReject() {
//        return false;
//    }

    abstract protected void accept();

    abstract protected void handleBuffer(Buffer buffer);

    abstract protected void handleException(Throwable throwable);

    abstract protected void handleEnd();

    protected final Future<Void> writeText(String text) {
        return webSocket.writeTextMessage(text);
    }

    protected final Future<Void> close() {
        return webSocket.close();
    }
}
