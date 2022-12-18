package io.github.sinri.keel.web;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;

public class KeelHttpServer {
    private final Keel keel;
    protected final HttpServer server;
    protected final Router router;
    protected final Boolean closeVertXWhenTerminated;
    protected KeelEventLogger logger;

    protected Handler<Promise<Object>> gracefulCloseHandler;

    public KeelHttpServer(
            Keel keel,
            HttpServerOptions options,
            Boolean closeVertXWhenTerminated
    ) {
        this.keel = keel;
        server = keel.getVertx().createHttpServer(options);
        router = Router.router(keel.getVertx());
        logger = keel.createOutputEventLogger(getClass().getName());
        this.closeVertXWhenTerminated = closeVertXWhenTerminated;
        this.gracefulCloseHandler = Promise::complete;
    }

    public KeelEventLogger getLogger() {
        return logger;
    }

    public void setLogger(KeelEventLogger logger) {
        this.logger = logger;
    }

    public Router getRouter() {
        return router;
    }

    /**
     * 给你一个router去安装routes吧！
     *
     * @since 2.8
     */
    public KeelHttpServer configureRoutes(Handler<Router> routeConfigureHandler) {
        routeConfigureHandler.handle(getRouter());
        return this;
    }

//    /**
//     * @since 2.4
//     */
//    public void setWebSocketHandlerToServer(Class<? extends KeelWebSocketHandler> handlerClass, DeploymentOptions deploymentOptions) {
//        // todo 魔改中
//        //this.server.webSocketHandler(websocket -> KeelWebSocketHandler.handle(websocket, handlerClass, getLogger(), deploymentOptions));
//    }

    public KeelHttpServer setGracefulCloseHandler(Handler<Promise<Object>> gracefulCloseHandler) {
        this.gracefulCloseHandler = gracefulCloseHandler;
        return this;
    }

    public void listen() {
        server.requestHandler(router)
                .exceptionHandler(throwable -> {
                    getLogger().exception(throwable, "KeelHttpServer Exception");
                })
                .listen(httpServerAsyncResult -> {
                    if (httpServerAsyncResult.succeeded()) {
                        HttpServer httpServer = httpServerAsyncResult.result();
                        logger.info("HTTP Server Established, Actual Port: " + httpServer.actualPort());
                    } else {
                        Throwable throwable = httpServerAsyncResult.cause();
                        logger.exception(throwable, "Listen failed");

                        if (closeVertXWhenTerminated) {
                            this.keel.gracefullyClose(gracefulCloseHandler)
                                    .onSuccess(v -> logger.info("VertX Instance Closed"))
                                    .onFailure(vertxCloseFailed -> logger.error("VertX Instance Closing Failure: " + vertxCloseFailed.getMessage()));
                        }
                    }
                })
        ;
    }

    public void terminate() {
        server.close(ar -> {
            if (ar.succeeded()) {
                logger.info("HTTP Server Closed");
            } else {
                logger.error("HTTP Server Closing Failure: " + ar.cause().getMessage());
            }

            if (closeVertXWhenTerminated) {
                this.keel.gracefullyClose(gracefulCloseHandler)
                        .onSuccess(v -> logger.info("VertX Instance Closed"))
                        .onFailure(vertxCloseFailed -> logger.error("VertX Instance Closing Failure: " + vertxCloseFailed.getMessage()));
            }
        });
    }
}
