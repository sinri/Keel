package io.github.sinri.keel.tesuto;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @see io.github.sinri.keel.tesuto.KeelTest
 * @since 3.0.10
 * Annotation used on the public methods (which should return {@code Future<Void>}) of the implement class of KeelTest.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TestUnit {
}
