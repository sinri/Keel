package io.github.sinri.keel.tesuto;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @see io.github.sinri.keel.tesuto.KeelTest
 * @since 3.0.10
 * Annotation used on the public methods (which should return {@code Future<Void>}) of the implement class of KeelTest.
 * @since 3.0.14 add skip.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TestUnit {
    /**
     * If it is set to true, the test method would be skipped.
     *
     * @since 3.0.14
     */
    boolean skip() default false;
}
