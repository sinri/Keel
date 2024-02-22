package io.github.sinri.keel.web.http;

import io.github.sinri.keel.logger.event.KeelEventLog;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

abstract public class KeelHttpServer extends KeelVerticleBase<KeelEventLog> {
    public static final String CONFIG_HTTP_SERVER_PORT = "http_server_port";
    public static final String CONFIG_HTTP_SERVER_OPTIONS = "http_server_options";
    public static final String CONFIG_IS_MAIN_SERVICE = "is_main_service";
    protected Handler<Promise<Object>> gracefulCloseHandler;
    protected HttpServer server;

    protected int getHttpServerPort() {
        return this.config().getInteger(CONFIG_HTTP_SERVER_PORT, 8080);
    }

    protected HttpServerOptions getHttpServerOptions() {
        JsonObject httpServerOptions = this.config().getJsonObject(CONFIG_HTTP_SERVER_OPTIONS);
        if (httpServerOptions == null) {
            return new HttpServerOptions()
                    .setPort(getHttpServerPort());
        } else {
            return new HttpServerOptions(httpServerOptions);
        }
    }

    protected boolean isMainService() {
        return this.config().getBoolean(CONFIG_IS_MAIN_SERVICE, true);
    }

    protected abstract void configureRoutes(Router router);

    public KeelHttpServer setGracefulCloseHandler(Handler<Promise<Object>> gracefulCloseHandler) {
        this.gracefulCloseHandler = gracefulCloseHandler;
        return this;
    }

    @Override
    public void start() throws Exception {
        setRoutineIssueRecorder(createRoutineIssueRecorder());

        this.server = Keel.getVertx().createHttpServer(getHttpServerOptions());

        Router router = Router.router(Keel.getVertx());
        this.configureRoutes(router);

        server.requestHandler(router)
                .exceptionHandler(throwable -> {
                    getRoutineIssueRecorder().exception(throwable, r -> r.message("KeelHttpServer Exception"));
                })
                .listen(httpServerAsyncResult -> {
                    if (httpServerAsyncResult.succeeded()) {
                        HttpServer httpServer = httpServerAsyncResult.result();
                        getRoutineIssueRecorder().info(r -> r.message("HTTP Server Established, Actual Port: " + httpServer.actualPort()));

//                        this.getKeel().addClosePrepareHandler(this::terminate);
                    } else {
                        Throwable throwable = httpServerAsyncResult.cause();
                        getRoutineIssueRecorder().exception(throwable, r -> r.message("Listen failed"));

                        if (this.isMainService()) {
                            Keel.gracefullyClose(Promise::complete);
                        }
                    }
                })
        ;
    }

    /**
     * @since 3.2.0
     */
    protected KeelIssueRecorder<KeelEventLog> createRoutineIssueRecorder() {
        String topic = getClass().getName();
        return KeelIssueRecordCenter.outputCenter().generateRecorder(topic, () -> new KeelEventLog(topic));
    }

    public void terminate(Promise<Void> promise) {
        server.close().andThen(ar -> {
                    if (ar.succeeded()) {
                        getRoutineIssueRecorder().info(r -> r.message("HTTP Server Closed"));
                        promise.complete();
                    } else {
                        getRoutineIssueRecorder().exception(ar.cause(), r -> r.message("HTTP Server Closing Failure: " + ar.cause().getMessage()));
                        promise.fail(ar.cause());
                    }
                })
                .andThen(ar -> {
                    if (this.isMainService()) {
                        Keel.gracefullyClose(Promise::complete);
                    }
                });
    }
}
