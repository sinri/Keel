package io.github.sinri.keel.test.lab.mysql;

import io.github.sinri.keel.facade.Keel;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

public class QueryTest {
    public static void main(String[] args) {
        Keel.initializeVertx(new VertxOptions())
                .compose(init -> {
                    return test();
                })
                .eventually(v -> Keel.getVertx().close());
    }

    private static Future<Void> test() {
        return Future.succeededFuture();
    }

}
