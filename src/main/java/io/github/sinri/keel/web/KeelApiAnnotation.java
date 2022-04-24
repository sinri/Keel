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
@Deprecated
public @interface KeelApiAnnotation {
    String[] acceptedRequestMethods() default {};

    String responseContentType() default "application/json";

    /**
     * used in KeelUrlRuleRouterKit only
     *
     * @return URL Rule
     * @since 1.13
     */
    String urlRule() default "";

    /**
     * used in KeelUrlRuleRouterKit only
     *
     * @return VIRTUAL HOST, full Domain or wildcards
     * @since 1.13
     */
    String virtualHost() default "";

    /**
     * @return if the method would end response routine
     * @since 1.13
     */
    boolean directlyOutput() default false;
}
