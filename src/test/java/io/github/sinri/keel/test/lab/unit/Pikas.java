package io.github.sinri.keel.test.lab.unit;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Pikas {
    Pika[] value();
}
