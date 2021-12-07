package io.github.sinri.Keel.web;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;

public abstract class HttpServerVerticle extends AbstractVerticle {
    private HttpServer server;

    public void start() {
        server = vertx.createHttpServer();
        initializeRequestHandler(server);
        server.listen(getListenPort());
    }

    // extension

    abstract protected void initializeRequestHandler(HttpServer server);

    abstract protected int getListenPort();

    abstract protected String getListenAddress();

}
