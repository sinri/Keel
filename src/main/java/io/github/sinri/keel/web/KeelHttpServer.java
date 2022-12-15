package io.github.sinri.keel.web;

import io.github.sinri.keel.lagecy.Keel;
import io.github.sinri.keel.lagecy.core.logger.KeelLogger;
import io.github.sinri.keel.web.websockets.KeelWebSocketHandler;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;

public class KeelHttpServer {
    protected final HttpServer server;
    protected final Router router;
    protected final Boolean closeVertXWhenTerminated;
    protected KeelLogger logger;

    public KeelHttpServer(
            HttpServerOptions options,
            Boolean closeVertXWhenTerminated
    ) {
        server = Keel.getVertx().createHttpServer(options);
        router = Router.router(Keel.getVertx());
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
     * 给你一个router去安装routes吧！
     *
     * @since 2.8
     */
    public KeelHttpServer configureRoutes(Handler<Router> routeConfigureHandler) {
        routeConfigureHandler.handle(getRouter());
        return this;
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
                            Keel.getVertx().close()
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
                Keel.getVertx().close()
                        .onSuccess(v -> logger.info("VertX Instance Closed"))
                        .onFailure(vertxCloseFailed -> logger.error("VertX Instance Closing Failure: " + vertxCloseFailed.getMessage()));
            }
        });
    }
}
