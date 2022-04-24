package io.github.sinri.keel.web.routing;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.web.KeelApiAnnotation;
import io.github.sinri.keel.web.KeelWebRequestController;
import io.github.sinri.keel.web.KeelWebRequestFilter;
import io.vertx.core.Future;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Auto install routes from a KeelWebRequestController.
 *
 * @since 1.13
 */
@Deprecated
public class KeelUrlRuleRouterKit extends KeelRouterKit {
    private final Router router;

    private KeelUrlRuleRouterKit(Router router) {
        this.router = router;
    }

    public static KeelUrlRuleRouterKit installToRouter(Router router) {
        return new KeelUrlRuleRouterKit(router);
    }

    public KeelUrlRuleRouterKit setLogger(KeelLogger logger) {
        this.logger = logger;
        return this;
    }

    public void registerClass(
            Class<? extends KeelWebRequestController> controllerClass,
            List<Class<? extends KeelWebRequestFilter>> filterClassList
    ) {
        Method[] methods = controllerClass.getMethods();
        for (var method : methods) {
            if (!isMethodPublicNonStaticNotInheritedAndReturnFuture(method)) continue;

            KeelApiAnnotation annotation = method.getAnnotation(KeelApiAnnotation.class);
            if (annotation == null) {
                continue;
            }

            // ok, it may be an api handler

            String urlRule = annotation.urlRule();
            Route route = router.route(urlRule);
            if (!annotation.virtualHost().equalsIgnoreCase("")) {
                route.virtualHost(annotation.virtualHost());
            }
            route.handler(ctx -> checkMethodAcceptable(annotation, ctx)
                    .compose(methodAcceptable -> filterForRequestedMethod(ctx, method, filterClassList))
                    .compose(filtered -> {
                        try {
                            KeelWebRequestController controller = controllerClass.getConstructor(RoutingContext.class).newInstance(ctx);
                            return (Future<?>) method.invoke(controller);
                        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                            return Future.failedFuture(e);
                        }
                    })
                    .compose(result -> {
                        if (annotation.directlyOutput()) {
                            return Future.succeededFuture();
                        } else {
                            return respond(annotation, ctx, result);
                        }
                    })
                    .onFailure(throwable -> {
                        logger.exception("Failure for request " + ctx.request(), throwable);
                        respond(annotation, ctx, throwable);
                    })
            );
        }
    }

}
