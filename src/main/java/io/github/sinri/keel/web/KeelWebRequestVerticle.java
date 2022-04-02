package io.github.sinri.keel.web;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.KeelHelper;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.VerticleAbleToUndeployItself;
import io.github.sinri.keel.web.routing.KeelRouterKit;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @since 1.14
 * @deprecated since 2.0 use KeelWebRequestReceptionist instead
 */
abstract public class KeelWebRequestVerticle extends AbstractVerticle implements VerticleAbleToUndeployItself {
    private final RoutingContext routingContext;
    private KeelLogger logger = KeelLogger.buildSilentLogger();

    public KeelWebRequestVerticle(RoutingContext routingContext, String loggingAspect) {
        this.routingContext = routingContext;

        if (loggingAspect == null) {
            loggingAspect = getDefaultAspectForLogging();
        }
        this.logger = Keel.standaloneLogger(loggingAspect);
    }

    public KeelLogger getLogger() {
        return logger;
    }

    private String getDefaultAspectForLogging() {
        var x = this.getClass().getName().split("\\.");
        if (x.length == 0) {
            return "KeelSyncWorkerVerticle";
        }
        return x[x.length - 1];
    }

    /**
     * 在开始静态代码之前进行准备工作：
     * 初始化日志记录器；
     * 打印传入的动态配置。
     */
    protected Future<Void> prepare() {
        logger.setCategoryPrefix(this.deploymentID());
        logger.info("prepare with config read from context", config());
        return Future.succeededFuture();
    }

    public RoutingContext getRoutingContext() {
        return routingContext;
    }

    @Override
    public void start() throws Exception {
        super.start();
        AtomicReference<KeelApiAnnotation> annotation = new AtomicReference<>();
        prepare()
                .compose(prepared -> {
                    try {
                        annotation.set(KeelHelper.getAnnotationOfMethod(
                                this.getClass().getMethod("handleRequest"),
                                KeelApiAnnotation.class,
                                getDefaultAnnotation()
                        ));
                        if (annotation.get() == null) {
                            return Future.failedFuture("Without KeelApiAnnotation");
                        }
                    } catch (NoSuchMethodException e) {
                        return Future.failedFuture(e);
                    }
                    return handleRequest();
                })
                .onComplete(asyncResult -> {
                    Object x;
                    if (asyncResult.succeeded()) {
                        x = asyncResult.result();
                    } else {
                        x = asyncResult.cause();
                    }
                    KeelRouterKit.respond(getLogger(), annotation.get(), getRoutingContext(), x)
                            .compose(done -> undeployMe());
                });
    }

    /**
     * 处理请求并返回作为回复报文内容的数据。
     * 首先
     *
     * @param <R> result type
     * @return future of R
     */
    abstract public <R> Future<R> handleRequest();

    @Override
    public Future<Void> undeployMe() {
        return VerticleAbleToUndeployItself.undeploy(deploymentID());
    }

    private KeelApiAnnotation getDefaultAnnotation() {
        return new KeelApiAnnotation() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return KeelApiAnnotation.class;
            }

            @Override
            public String[] acceptedRequestMethods() {
                return new String[0];
            }

            @Override
            public String responseContentType() {
                return "application/json";
            }

            @Override
            public String urlRule() {
                return "";
            }

            @Override
            public String virtualHost() {
                return "";
            }

            @Override
            public boolean directlyOutput() {
                return false;
            }
        };
    }
}
