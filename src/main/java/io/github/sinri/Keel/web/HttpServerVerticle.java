package io.github.sinri.Keel.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;

public abstract class HttpServerVerticle extends AbstractVerticle {
    private HttpServer server;

    public void start() {
        server = vertx.createHttpServer();
        initializeRequestHandler(server);
        server.listen(getListenPort());
    }

    public void start(Future<Void> startFuture) {
        server = vertx.createHttpServer();
        initializeRequestHandler(server);

        String listenAddress = getListenAddress();
        if (listenAddress == null) {
            server.listen(getListenPort(), res -> {
                if (res.succeeded()) {
                    startFuture.complete();
                } else {
                    startFuture.fail(res.cause());
                }
            });
        } else {
            server.listen(getListenPort(), listenAddress, res -> {
                if (res.succeeded()) {
                    startFuture.complete();
                } else {
                    startFuture.fail(res.cause());
                }
            });
        }
    }

    // extension

    abstract protected void initializeRequestHandler(HttpServer server);

    abstract protected int getListenPort();

    abstract protected String getListenAddress();

}
