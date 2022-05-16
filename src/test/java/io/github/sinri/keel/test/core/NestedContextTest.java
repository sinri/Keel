package io.github.sinri.keel.test.core;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.KeelHelper;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;

import java.util.Set;


public class NestedContextTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        new V1().deployMe();
    }

    public static class V1 extends KeelVerticle {
        @Override
        public void start() throws Exception {
            super.start();

            context.put("content_hash", "v1 - " + deploymentID());

            System.out.println("v1.content_hash: " + context.get("content_hash"));

            new V2().deployMe()
                    .compose(v2DeploymentID -> {
                        System.out.println("v2 deployed by v1 " + deploymentID());

                        Keel.getVertx().setPeriodic(100L, timerID -> {
                            Set<String> strings = Keel.getVertx().deploymentIDs();
                            System.out.println("deploymentIDs: " + KeelHelper.joinStringArray(strings, ";"));
                            if (!strings.contains(v2DeploymentID)) {
                                System.out.println("v2 undeployed, ends");
                                Keel.getVertx().close();
                            }
                        });

                        return Future.succeededFuture();
                    });
        }
    }

    public static class V2 extends KeelVerticle {
        @Override
        public void start() throws Exception {
            super.start();

            System.out.println("before v2.content_hash: " + context.get("content_hash"));

            context.put("content_hash", "v2 - " + deploymentID());

            System.out.println("after v2.content_hash: " + context.get("content_hash"));

            Keel.getVertx().setTimer(200L, timer -> {
                undeployMe()
                        .compose(undeployMeDone -> {
                            System.out.println("v2 undeployed");
                            return Future.succeededFuture();
                        })
                        .recover(throwable -> {
                            throwable.printStackTrace();
                            return Future.succeededFuture();
                        });
            });

        }
    }
}
