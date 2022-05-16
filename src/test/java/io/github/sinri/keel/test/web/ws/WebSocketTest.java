package io.github.sinri.keel.test.web.ws;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.web.websockets.KeelWebSocketHandler;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.ServerWebSocket;

public class WebSocketTest extends KeelWebSocketHandler {

    public WebSocketTest(ServerWebSocket webSocket) {
        super(webSocket);
    }

    @Override
    protected KeelLogger prepareLogger() {
        return Keel.standaloneLogger("WebSocketTest");
    }

    @Override
    protected Future<String> sendTextMessageAfterAccept() {
        return Future.succeededFuture("ACCEPTED!");
    }

    @Override
    protected void handleBuffer(Buffer buffer) {
//        System.out.println("WebSocketTest::handleBuffer logger is " + getLogger().getUniqueLoggerID());
        getLogger().info("buffer comes: " + buffer);
        getWebSocket().writeTextMessage(buffer.toString() + " +1");
    }
}
