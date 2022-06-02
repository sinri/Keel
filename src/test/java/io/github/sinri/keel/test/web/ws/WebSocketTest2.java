package io.github.sinri.keel.test.web.ws;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger2.KeelLogger;
import io.github.sinri.keel.web.websockets.KeelWebSocketHandler;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;

import java.util.Date;

public class WebSocketTest2 extends KeelWebSocketHandler {
    public WebSocketTest2(ServerWebSocket webSocket) {
        super(webSocket);
    }

    @Override
    protected KeelLogger prepareLogger() {
        String requestID = new Date().getTime() + "-" + deploymentID();
        return Keel.standaloneLogger("WebSocketTest2").setCategoryPrefix(requestID);
    }

    @Override
    protected void accept() {
        getLogger().info("WebSocketTest2::accept");
        this.writeText("WELCOME");
    }

    @Override
    protected void handleBuffer(Buffer buffer) {
        getLogger().info("WebSocketTest2::handleBuffer " + buffer.toString());
        this.writeText(buffer + " +1");
    }

    @Override
    protected void handleException(Throwable throwable) {
        getLogger().exception("WebSocketTest2::handleException ", throwable);

        close();
    }

    @Override
    protected void handleEnd() {
        getLogger().fatal("WebSocketTest2::handleEnd");
        undeployMe()
                .compose(v -> {
                    getLogger().info("undeployMe over");
                    return Future.succeededFuture();
                });
    }
}
