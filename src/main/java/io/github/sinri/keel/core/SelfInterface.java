package io.github.sinri.keel.core;

import javax.annotation.Nonnull;

/**
 * For the "return self" trick, let the interface could define a self return method to make chain call available.
 *
 * @param <T> The final implementation class.
 * @since 3.1.10
 */
public interface SelfInterface<T> {
    @Nonnull
    T getImplementation();
}
