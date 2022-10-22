package io.github.sinri.keel.servant.intravenous.legacy;

import io.vertx.core.Future;

/**
 * 接受静脉滴注的人体
 *
 * @param <R> 药效
 * @param <D> 点滴
 * @since 2.7
 */
@Deprecated(since = "2.9", forRemoval = true)
public interface KeelIntravenousConsumer<R, D extends KeelIntravenousDrop> {
    Future<KeelIntravenousTaskConclusion<R>> handle(D drop);
}
