package io.github.sinri.keel.servant.intravenous.injection;

import io.github.sinri.keel.servant.intravenous.KeelIntravenousConsumer;
import io.github.sinri.keel.servant.intravenous.KeelIntravenousTaskConclusion;
import io.vertx.core.Future;

/**
 * @since 2.8
 */
class InjectionConsumer implements KeelIntravenousConsumer<Object, InjectionDrop> {
    @Override
    public Future<KeelIntravenousTaskConclusion<Object>> handle(InjectionDrop drop) {
        return drop.handle()
                .compose(Future::succeededFuture);
    }
}
