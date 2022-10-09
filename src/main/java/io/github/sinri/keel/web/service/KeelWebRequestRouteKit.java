package io.github.sinri.keel.web.service;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.web.ApiMeta;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.TimeoutHandler;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @param <S>
 * @since 2.8.1
 */
public class KeelWebRequestRouteKit<S extends KeelWebRequestHandler> {
    private final Router router;
    private final Class<S> classOfService;
    private final List<Handler<RoutingContext>> extraPreHandlers = new ArrayList<>();
    private int timeoutStatusCode = 509;
    private String uploadDirectory = BodyHandler.DEFAULT_UPLOADS_DIRECTORY;

    public KeelWebRequestRouteKit(Class<S> classOfService) {
        this.classOfService = classOfService;
        router = Router.router(Keel.getVertx());
    }

    public KeelWebRequestRouteKit(Class<S> classOfService, Router router) {
        this.classOfService = classOfService;
        this.router = router;
    }

    public KeelWebRequestRouteKit<S> setTimeoutStatusCode(int timeoutStatusCode) {
        this.timeoutStatusCode = timeoutStatusCode;
        return this;
    }

    public KeelWebRequestRouteKit<S> setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
        return this;
    }

    public KeelWebRequestRouteKit<S> addExtraPreHandler(Handler<RoutingContext> handler) {
        this.extraPreHandlers.add(handler);
        return this;
    }

    /**
     * Load all classes inside the given package, and filter out those with ApiMeta, to build routes for them.
     *
     * @param packageName such as "com.leqee.spore.service"
     */
    public KeelWebRequestRouteKit<S> loadPackage(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends S>> allClasses = reflections.getSubTypesOf(classOfService);

        allClasses.forEach(c -> {
            ApiMeta apiMeta = Keel.reflectionHelper().getAnnotationOfClass(c, ApiMeta.class);
            if (apiMeta == null) return;
            S handler;
            try {
                handler = c.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                return;
            }

            Route route = router.route(apiMeta.routePath());

            if (apiMeta.timeout() > 0) {
                route.handler(TimeoutHandler.create(apiMeta.timeout(), timeoutStatusCode));
            }

            if (apiMeta.allowMethods() != null) {
                for (var methodName : apiMeta.allowMethods()) {
                    route.method(HttpMethod.valueOf(methodName));
                }
            }
            if (apiMeta.virtualHost() != null && !apiMeta.virtualHost().equals("")) {
                route.virtualHost(apiMeta.virtualHost());
            }

            route.handler(BodyHandler.create(uploadDirectory));

            if (!extraPreHandlers.isEmpty()) {
                extraPreHandlers.forEach(route::handler);
            }

            route.handler(handler);
        });

        return this;
    }

    public Router getRouter() {
        return router;
    }
}
