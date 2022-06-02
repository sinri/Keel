package io.github.sinri.keel.web;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger2.KeelLogger;
import io.github.sinri.keel.web.websockets.KeelWebSocketHandler;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;

public class KeelHttpServer {
    protected final Vertx vertx;
    protected final HttpServer server;
    protected final Router router;

    protected KeelLogger logger;

    protected final Boolean closeVertXWhenTerminated;

    public KeelHttpServer(
            Vertx vertx,
            HttpServerOptions options,
            Boolean closeVertXWhenTerminated
    ) {
        this.vertx = vertx;
        server = vertx.createHttpServer(options);
        router = Router.router(vertx);
        logger = Keel.outputLogger(getClass().getName());
        this.closeVertXWhenTerminated = closeVertXWhenTerminated;
    }

    public KeelLogger getLogger() {
        return logger;
    }

    public void setLogger(KeelLogger logger) {
        this.logger = logger;
    }

    public Router getRouter() {
        return router;
    }

    /**
     * @since 2.4
     */
    public void setWebSocketHandlerToServer(Class<? extends KeelWebSocketHandler> handlerClass, DeploymentOptions deploymentOptions) {
        this.server.webSocketHandler(websocket -> KeelWebSocketHandler.handle(websocket, handlerClass, getLogger(), deploymentOptions));
    }

    public void listen() {
        server.requestHandler(router)
                .exceptionHandler(throwable -> {
                    getLogger().exception("KeelHttpServer Exception", throwable);
                })
                .listen(httpServerAsyncResult -> {
                    if (httpServerAsyncResult.succeeded()) {
                        HttpServer httpServer = httpServerAsyncResult.result();
                        logger.info("HTTP Server Established, Actual Port: " + httpServer.actualPort());
                    } else {
                        Throwable throwable = httpServerAsyncResult.cause();
                        logger.exception("Listen failed", throwable);

                        if (closeVertXWhenTerminated) {
                            vertx.close()
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
                vertx.close()
                        .onSuccess(v -> logger.info("VertX Instance Closed"))
                        .onFailure(vertxCloseFailed -> logger.error("VertX Instance Closing Failure: " + vertxCloseFailed.getMessage()));
            }
        });
    }
}
