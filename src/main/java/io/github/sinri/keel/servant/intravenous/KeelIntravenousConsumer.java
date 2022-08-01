package io.github.sinri.keel.servant.intravenous;

import io.vertx.core.Future;

/**
 * 接受静脉滴注的人体
 *
 * @param <R> 药效
 * @param <D> 点滴
 * @since 2.7
 */
public interface KeelIntravenousConsumer<R, D extends KeelIntravenousDrop> {
    Future<KeelIntravenousTaskConclusion<R>> handle(D drop);
}
