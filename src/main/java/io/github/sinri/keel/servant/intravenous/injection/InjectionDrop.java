package io.github.sinri.keel.servant.intravenous.injection;

import io.github.sinri.keel.servant.intravenous.KeelIntravenousDrop;
import io.github.sinri.keel.servant.intravenous.KeelIntravenousTaskConclusion;
import io.vertx.core.Future;

import java.util.function.Function;

/**
 * @since 2.8
 */
class InjectionDrop implements KeelIntravenousDrop {
    private final String reference;
    private final Function<String, Future<KeelIntravenousTaskConclusion<Object>>> function;

    public InjectionDrop(String reference, Function<String, Future<KeelIntravenousTaskConclusion<Object>>> function) {
        this.reference = reference;
        this.function = function;
    }

    @Override
    public String getReference() {
        return reference;
    }

    public Future<KeelIntravenousTaskConclusion<Object>> handle() {
        return function.apply(getReference());
    }
}
