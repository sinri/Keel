package io.github.sinri.keel.servant.intravenous.injection;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.servant.intravenous.KeelIntravenous;
import io.github.sinri.keel.servant.intravenous.KeelIntravenousTaskConclusion;
import io.vertx.core.Future;

import java.util.function.Function;

/**
 * @since 2.8
 */
public class KeelInjection {
    private final KeelIntravenous<Object, InjectionDrop> intravenous;

    public KeelInjection() {
        this.intravenous = new KeelIntravenous<Object, InjectionDrop>(
                new InjectionConsumer()
        );
    }

    public KeelInjection(KeelLogger logger) {
        this.intravenous = new KeelIntravenous<Object, InjectionDrop>(
                new InjectionConsumer()
        );
        this.intravenous.setLogger(logger);
    }

    protected KeelIntravenous<Object, InjectionDrop> getIntravenous() {
        return intravenous;
    }


    public Future<String> deploy() {
        return this.intravenous.deployMe();
    }

//    public void drip(String reference, Function<String, Future<KeelIntravenousTaskConclusion<Object>>> function) {
//        this.intravenous.drip(new InjectionDrop(reference, function));
//    }

    public void drip(String reference, Function<String, Future<Void>> function) {
        this.intravenous.drip(new InjectionDrop(reference, r -> {
            this.intravenous.getLogger().info("TASK [" + r + "] START");
            return function.apply(r)
                    .compose(done -> {
                        this.intravenous.getLogger().info("TASK [" + r + "] DONE");
                        return Future.succeededFuture(
                                KeelIntravenousTaskConclusion.createForObject(r, true, "Done", null)
                        );
                    })
                    .recover(throwable -> {
                        this.intravenous.getLogger().exception("TASK [" + r + "] FAILED", throwable);
                        return Future.succeededFuture(
                                KeelIntravenousTaskConclusion.createForObject(r, false, throwable.getClass() + ": " + throwable.getMessage(), null)
                        );
                    });
        }));
    }
}
