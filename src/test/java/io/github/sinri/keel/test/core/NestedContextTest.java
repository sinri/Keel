package io.github.sinri.keel.test.core;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;
import io.vertx.core.file.OpenOptions;

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
            setLogger(Keel.outputLogger("v1"));

            context.put("content_hash", "v1 - " + deploymentID());

            getLogger().info("v1.content_hash: " + context.get("content_hash"));

            new V2().deployMe()
                    .compose(v2DeploymentID -> {
                        getLogger().info("v2 deployed by v1 " + deploymentID());

                        Keel.getVertx().setPeriodic(100L, timerID -> {
                            getLogger().info("in periodic of v1");
                            Set<String> strings = Keel.getVertx().deploymentIDs();
                            getLogger().info("deploymentIDs: " + Keel.helpers().string().joinStringArray(strings, ";"));
                            if (!strings.contains(v2DeploymentID)) {
                                getLogger().info("v2 undeployed, ends");
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
            setLogger(Keel.outputLogger("v2"));

            getLogger().info("before v2.content_hash: " + context.get("content_hash"));

            context.put("content_hash", "v2 - " + deploymentID());

            getLogger().info("after v2.content_hash: " + context.get("content_hash"));

            Keel.getVertx()
                    .fileSystem()
                    .open("/Users/leqee/code/Keel/LICENSE", new OpenOptions().setRead(true))
                    .compose(asyncFile -> {
                        asyncFile.handler(buffer -> {
                            getLogger().info("v2 read file " + buffer.length() + " bytes");
                        });
                        return Future.succeededFuture();
                    })
                    .compose(v -> {
                        getLogger().info("whahahaha v2");
                        return Future.succeededFuture();
                    });


            Keel.getVertx().setTimer(200L, timer -> {
                getLogger().info("in timer of v2");
                undeployMe()
                        .compose(undeployMeDone -> {
                            getLogger().info("v2 undeployed");
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
