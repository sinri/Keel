package io.github.sinri.keel.test.v1.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AnnoI {
    String fieldName();
}
