package io.github.sinri.keel.servant.sundial;

import io.github.sinri.keel.verticles.KeelVerticleImplPure;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;

/**
 * @since 3.2.4
 * @since 3.2.5 Used in KeelSundial
 */
public class KeelSundialVerticle extends KeelVerticleImplPure {
    private final KeelSundialPlan sundialPlan;
    private final Calendar now;

    public KeelSundialVerticle(@NotNull KeelSundialPlan sundialPlan, Calendar now) {
        this.sundialPlan = sundialPlan;
        this.now = now;
    }

    @Override
    protected void startAsPureKeelVerticle() {
        Future.succeededFuture()
                .compose(v -> {
                    return sundialPlan.execute(now);
                })
                .onComplete(ar -> {
                    undeployMe();
                });
    }
}
