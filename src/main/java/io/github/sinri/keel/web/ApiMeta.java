package io.github.sinri.keel.web;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @since 2.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ApiMeta {
    String virtualHost() default "";

    String routePath();

    String[] allowMethods() default {"POST"};

    boolean requestBodyNeeded() default true;

    /**
     * @return timeout in ms. default is 10s. if 0, no timeout.
     * @since 2.9
     */
    long timeout() default 10_000;

    /**
     * @return the HTTP RESPONSE STATUS CODE for timeout.
     * @since 2.9
     */
    int statusCodeForTimeout() default 509;

    /**
     * Cross Origin Resource Sharing
     *
     * @return "" as NOT ALLOWED, "*" as ALLOW ALL, else as DOMAIN REGEX PATTERN
     * @since 2.9
     */
    String corsOriginPattern() default "";
}