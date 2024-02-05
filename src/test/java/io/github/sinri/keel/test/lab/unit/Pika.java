package io.github.sinri.keel.test.lab.unit;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Pikas.class)
public @interface Pika {
    String value();
}
