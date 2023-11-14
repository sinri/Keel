package io.github.sinri.keel.core;

import java.lang.annotation.*;

/**
 * This annotation is designed for Keel codes that not fully tested.
 *
 * @since 3.0.10
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER,
        ElementType.CONSTRUCTOR, ElementType.LOCAL_VARIABLE, ElementType.PACKAGE,
        ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE, ElementType.TYPE_PARAMETER
})
public @interface TechnicalPreview {
    String since() default "";

    String notice() default "";
}
