package io.github.sinri.keel.test.lab.unit;

import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

public class HikaruTest extends KeelTest {
    @TestUnit
    public Future<Void> testHikaruForPikas() {
        Pikas all = KeelHelpers.reflectionHelper().getAnnotationOfClass(Hikaru.class, Pikas.class);
        for (Pika pika : all.value()) {
            logger().info("testHikaruForPikas: Each Pika of Hikaru: " + pika.value());
        }
        return Future.succeededFuture();
    }

    @TestUnit
    public Future<Void> testHikaruForPika() {
        Pika[] annotationsOfClass = KeelHelpers.reflectionHelper().getAnnotationsOfClass(Hikaru.class, Pika.class);
        for (Pika pika : annotationsOfClass) {
            logger().info("testHikaruForPika: Each Pika of Hikaru: " + pika.value());
        }
        return Future.succeededFuture();
    }
}
