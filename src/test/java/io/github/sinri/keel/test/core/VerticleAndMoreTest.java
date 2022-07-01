package io.github.sinri.keel.test.core;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.servant.intravenous.injection.KeelInjection;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;

public class VerticleAndMoreTest {
    private static KeelInjection injection;

    public static void main(String[] args) {
        SharedTestBootstrap.initialize();
        injection = new KeelInjection();
        injection.deploy();

        new V1().deployMe();

        Keel.getVertx().setTimer(8000L, timer -> {
            Keel.getVertx().close();
        });
    }

    public static class V1 extends KeelVerticle {
        @Override
        public void start() throws Exception {
            Keel.outputLogger("v1").info("inside v1 " + this.deploymentID());

//            Keel.getVertx().setTimer(5000L,timer->{
//                Keel.outputLogger("v1").info("v1 timer");
//            });

            injection.drip(
                    "v1",
                    reference -> {
                        new V2().deployMe();
//                        return Future.succeededFuture();
                        return Future.failedFuture("111");
                    }
            );

            Keel.getVertx().setTimer(3000L, timer -> {
                this.undeployMe()
                        .onSuccess(v -> {
                            Keel.outputLogger("v1").info("undeploy v1");
                        })
                        .onFailure(throwable -> {
                            Keel.outputLogger("v1").exception("undeploy v1", throwable);
                        });
            });
        }
    }

    public static class V2 extends KeelVerticle {
        @Override
        public void start() throws Exception {
            super.start();
            Keel.getVertx().setTimer(5000L, timer -> {
                Keel.outputLogger("v2").info("v2 timer");
                this.undeployMe()
                        .onSuccess(v -> {
                            Keel.outputLogger("v2").info("undeploy v2");
                        })
                        .onFailure(throwable -> {
                            Keel.outputLogger("v2").exception("undeploy v2", throwable);
                        });
            });
        }
    }
}
