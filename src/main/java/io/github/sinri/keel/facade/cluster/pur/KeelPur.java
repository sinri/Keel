package io.github.sinri.keel.facade.cluster.pur;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.facade.cluster.pur.config.KeelPurConfig;
import io.github.sinri.keel.facade.cluster.pur.mixin.KeelPurClientMixin;
import io.github.sinri.keel.facade.cluster.pur.mixin.KeelPurServerMixin;
import io.github.sinri.keel.logger.event.KeelEventLogger;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.client.WebClient;

import javax.annotation.Nullable;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.1.3
 */
@TechnicalPreview(since = "3.1.3")
public class KeelPur extends KeelVerticleBase implements KeelPurServerMixin, KeelPurClientMixin {
    private final KeelPurConfig config;
    private KeelPurNodeManager nodeManager;
    private @Nullable HttpServer server;
    private @Nullable WebClient webClient;
    private boolean stopped = true;

    public KeelPur(KeelPurConfig config) {
        this.config = config;
    }

    @Override
    public KeelPurConfig getConfig() {
        return config;
    }

    public KeelPurNodeInfo getLocalNodeInfo() {
        return nodeManager.getLocalNodeInfo();
    }

    @Override
    public KeelPurNodeManager getNodeManager() {
        return nodeManager;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        KeelEventLogger logger = KeelOutputEventLogCenter.getInstance().createLogger("KeelPur");
        setLogger(logger);
        // 1. generate a UUID as node ID -> done in node manager
        this.nodeManager = new KeelPurNodeManager();
        // 2. Seek Friends
        // 2.1. Set up server to receive requirements
        // 2.2. Set up clients to send requirements
        this.stopped = false;
        this.startServer()
                .compose(server -> {
                    this.server = server;
                    return Future.succeededFuture();
                })
                .compose(serverDone -> {
                    this.webClient = WebClient.create(Keel.getVertx());
                    return startClients();
                })
                .andThen(startPromise);
    }


    @Override
    public void stop(Promise<Void> stopPromise) throws Exception {
        this.stopped = true;
        Future.succeededFuture()
                .compose(v -> {
                    if (this.webClient != null) {
                        this.webClient.close();
                    }
                    if (this.server != null) {
                        return this.stopServer(server);
                    } else {
                        return Future.succeededFuture();
                    }
                })
                .onComplete(ar -> {
                    this.nodeManager.close();
                    stopPromise.complete();
                });
    }


    @Override
    @Nullable
    public WebClient getClient() {
        return webClient;
    }

    @Override
    public boolean shouldStopClient() {
        return stopped;
    }
}
