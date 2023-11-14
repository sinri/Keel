package io.github.sinri.keel.test.lab.launcher;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.logger.event.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.verticles.KeelVerticleBase;
import io.vertx.core.Future;

public class TestMainVerticle extends KeelVerticleBase {


    @Override
    public void start() throws Exception {
        setLogger(KeelOutputEventLogCenter.instantLogger());

        KeelAsyncKit.endless(() -> {
            getLogger().info("X");
            return Future.succeededFuture();
        });
    }
}
