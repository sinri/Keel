package io.github.sinri.keel.web.tcp;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.servant.funnel.KeelFunnel;
import io.github.sinri.keel.servant.funnel.KeelFunnelImpl;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.NetSocket;
import io.vertx.core.net.SocketAddress;

import java.util.UUID;

/**
 * @since 2.8
 */
abstract public class KeelAbstractSocketWrapper {
    private final String socketID;
    private final NetSocket socket;
    private final KeelFunnel sisiodosi;
    private KeelLogger logger;

    public KeelAbstractSocketWrapper(NetSocket socket) {
        this(socket, UUID.randomUUID().toString());
    }

    public KeelAbstractSocketWrapper(NetSocket socket, String socketID) {
        this.socketID = socketID;
        this.socket = socket;
        this.logger = KeelLogger.silentLogger();
        this.logger.setCategoryPrefix(socketID);
        this.sisiodosi = KeelFunnel.getOneInstanceToDeploy(new KeelFunnelImpl.Options()
                .setQueryInterval(10L)
                .setTimeThreshold(100L)
                .setSizeThreshold(4)
        );
        this.sisiodosi.deployMe(new DeploymentOptions().setWorker(true));

        this.socket
                .handler(buffer -> {
                    getLogger().info("READ BUFFER " + buffer.length() + " BYTES");
                    getLogger().buffer(buffer);
                    this.sisiodosi.drop(() -> whenBufferComes(buffer)
                            .compose(v -> Future.succeededFuture()));
                })
                .endHandler(end -> {
                    /*
                     Set an end handler.
                     Once the stream has ended, and there is no more data to be read,
                     this handler will be called.
                     This handler might be called after the close handler
                     when the socket is paused and there are still buffers to deliver.
                     */
                    getLogger().info("READ TO END");
                    whenReadToEnd();
                })
                .drainHandler(drain -> {
                    /*
                    Set a drain handler on the stream.
                    If the write queue is full,
                    then the handler will be called when the write queue is ready to accept buffers again.
                    See Pipe for an example of this being used.
                    The stream implementation defines when the drain handler,
                    for example it could be when the queue size has been reduced to maxSize / 2.
                     */
                    getLogger().info("BE WRITABLE AGAIN, RESUME");
                    socket.resume();
                    whenDrain();
                })
                .closeHandler(close -> {
                    getLogger().info("CLOSE");
                    this.sisiodosi.undeployMe();
                    whenClose();
                })
                .exceptionHandler(throwable -> {
                    getLogger().exception("EXCEPTION", throwable);
                    whenExceptionOccurred(throwable);
                });
    }

    public KeelLogger getLogger() {
        return logger;
    }

    public KeelAbstractSocketWrapper setLogger(KeelLogger logger) {
        this.logger = logger;
        this.logger.setCategoryPrefix(socketID);
        return this;
    }

    /**
     * Do not use raw instance of NetSocket.
     */
    @Deprecated
    protected NetSocket getSocket() {
        return socket;
    }

    public String getSocketID() {
        return socketID;
    }

    /**
     * This method is managed by KeelSisiodosi, would be run in order and thread safely.
     */
    abstract protected Future<Void> whenBufferComes(Buffer incomingBuffer);

    protected void whenReadToEnd() {

    }

    protected void whenDrain() {

    }

    protected void whenClose() {

    }

    protected void whenExceptionOccurred(Throwable throwable) {

    }

    public Future<Void> write(String s) {
        Future<Void> future = this.socket.write(s);
        if (this.socket.writeQueueFull()) {
            this.socket.pause();
            getLogger().info("Write Queue Full, PAUSE");
        }
        return future;
    }

    public Future<Void> write(String s, String enc) {
        Future<Void> future = this.socket.write(s, enc);
        if (this.socket.writeQueueFull()) {
            this.socket.pause();
            getLogger().info("Write Queue Full, PAUSE");
        }
        return future;
    }

    public Future<Void> write(Buffer buffer) {
        Future<Void> future = this.socket.write(buffer);
        if (this.socket.writeQueueFull()) {
            this.socket.pause();
            getLogger().info("Write Queue Full, PAUSE");
        }
        return future;
    }

    public SocketAddress getRemoteAddress() {
        return this.socket.remoteAddress();
    }

    public SocketAddress getLocalAddress() {
        return this.socket.localAddress();
    }

    public String getRemoteAddressString() {
        return this.socket.remoteAddress().host() + ":" + this.socket.remoteAddress().port();
    }

    public String getLocalAddressString() {
        return this.socket.localAddress().host() + ":" + this.socket.localAddress().port();
    }

    public Future<Void> close() {
        return this.socket.close();
    }

    public KeelAbstractSocketWrapper setMaxSize(int maxSize) {
        this.socket.setWriteQueueMaxSize(maxSize);
        return this;
    }
}
