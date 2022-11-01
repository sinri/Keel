package io.github.sinri.keel.test.hazelcast;

import io.github.sinri.keel.servant.maxim.MaximBullet;
import io.vertx.core.Future;

public class BulletA extends MaximBullet {
    public String getX() {
        return this.readString("x");
    }

    public BulletA setX(String x) {
        this.jsonObject.put("x", x);
        return this;
    }

    @Override
    public Future<Void> fire() {
        if (getX() == null) {
            return Future.failedFuture(new NullPointerException());
        }
        return Future.succeededFuture();
    }
}
