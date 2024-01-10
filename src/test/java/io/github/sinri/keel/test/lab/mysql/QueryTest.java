package io.github.sinri.keel.test.lab.mysql;

import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class QueryTest {
    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions())
                .compose(init -> {
                    return test();
                })
                .eventually(() -> Keel.getVertx().close());
    }

    private static Future<Void> test() {
        return Future.succeededFuture();
    }

}
