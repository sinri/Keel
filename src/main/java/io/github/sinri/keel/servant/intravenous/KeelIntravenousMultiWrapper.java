package io.github.sinri.keel.servant.intravenous;

import io.vertx.core.Future;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @since 2.7
 */
abstract public class KeelIntravenousMultiWrapper<Hash, Result, Drop extends KeelIntravenousDrop> {
    private final Map<Hash, KeelIntravenous<Result, Drop>> intravenousMap = new ConcurrentHashMap<>();

    private final KeelIntravenousConsumer<Result, Drop> consumer;

    public KeelIntravenousMultiWrapper(KeelIntravenousConsumer<Result, Drop> consumer) {
        this.consumer = consumer;
    }

    abstract protected Future<Hash> computeHashForDrop(Drop drop);

    public Future<Void> drip(Drop drop) {
        return this.computeHashForDrop(drop)
                .compose(hash -> {
                    KeelIntravenous<Result, Drop> resultDropKeelIntravenous = this.ensureIntravenousForHash(hash);
                    resultDropKeelIntravenous.drip(drop);
                    return Future.succeededFuture();
                });
    }

    private KeelIntravenous<Result, Drop> ensureIntravenousForHash(Hash hash) {
        return this.intravenousMap.computeIfAbsent(hash, h -> new KeelIntravenous<>(this.consumer));
    }

    // notice: should there be any methods to decrease some map entries?
}
