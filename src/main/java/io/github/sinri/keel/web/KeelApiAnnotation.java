package io.github.sinri.keel.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @since 1.9
 * Used to annotate the methods in KeelWebRequestController.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface KeelApiAnnotation {
    String[] acceptedRequestMethods() default {};

    String responseContentType() default "application/json";
}
