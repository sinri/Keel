package io.github.sinri.keel.web;

import io.github.sinri.keel.core.logger.KeelLogger;
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
        logger = new KeelLogger();
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

    public void listen() {
        server.requestHandler(router)
                .exceptionHandler(throwable -> {
                    getLogger().exception("KeelHttpServer Exception", throwable);
//                    System.err.println("KeelHttpServer Exception: "+throwable.getMessage());
//                    throwable.printStackTrace();
                })
                .listen(httpServerAsyncResult -> {
                    if (httpServerAsyncResult.succeeded()) {
                        HttpServer httpServer = httpServerAsyncResult.result();
                        logger.info("HTTP Server Established: " + httpServer.toString() + " Actual Port: " + httpServer.actualPort());
                    } else {
                        Throwable throwable = httpServerAsyncResult.cause();
                        logger.fatal(throwable.getMessage());

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
