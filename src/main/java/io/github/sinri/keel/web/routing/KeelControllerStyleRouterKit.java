package io.github.sinri.keel.web.routing;

import io.github.sinri.keel.core.KeelHelper;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.web.KeelApiAnnotation;
import io.github.sinri.keel.web.KeelWebRequestController;
import io.github.sinri.keel.web.KeelWebRequestFilter;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Seek for <code>CONTROLLER::METHOD</code> with <code>HTTP::REQUEST</code>,
 * which is limited to a scope, such as a URL PATH PREFIX with wildcards.
 *
 * @since 1.1
 */
public class KeelControllerStyleRouterKit extends KeelRouterKit {
    private final String pathPrefix;
    private final String controllerPackage;
    private final List<Class<? extends KeelWebRequestFilter>> filterClassList;


    /**
     * @param pathPrefix        such as '/api/'
     * @param controllerPackage such as "com.leqee.oc.tachiba.handler"
     * @since 1.10
     */
    private KeelControllerStyleRouterKit(String pathPrefix, String controllerPackage) {
        this.controllerPackage = controllerPackage;
        this.filterClassList = new ArrayList<>();
        this.pathPrefix = pathPrefix;
    }

    public static KeelControllerStyleRouterKit installToRouter(Router router, String pathPrefix, String controllerPackage) {
        return new KeelControllerStyleRouterKit(pathPrefix, controllerPackage).installToRouter(router);
    }

    public static KeelApiAnnotation getKeelApiAnnotationForMethod(Method method) {
        return KeelHelper.getAnnotationOfMethod(method, KeelApiAnnotation.class, new KeelApiAnnotation() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return this.getClass();
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
                return null;
            }

            @Override
            public String virtualHost() {
                return null;
            }

            @Override
            public boolean directlyOutput() {
                return false;
            }
        });
    }

    private KeelControllerStyleRouterKit installToRouter(Router router) {
        String routePath = pathPrefix;
        if (!routePath.endsWith("/")) {
            routePath = routePath + "/";
        }
        router.route(routePath + "*")
                .handler(BodyHandler.create())
                .handler(this::processRouterRequest);
        return this;
    }

    public KeelLogger getLogger() {
        return logger;
    }

    public KeelControllerStyleRouterKit setLogger(KeelLogger logger) {
        this.logger = logger;
        return this;
    }

    public KeelControllerStyleRouterKit addFilter(Class<? extends KeelWebRequestFilter> filterClass) {
        this.filterClassList.add(filterClass);
        return this;
    }

    public KeelControllerStyleRouterKit addFilters(Collection<Class<? extends KeelWebRequestFilter>> filterClassCollection) {
        this.filterClassList.addAll(filterClassCollection);
        return this;
    }

    public void processRouterRequest(RoutingContext ctx) {
        String requestPath = ctx.request().path();
        String requestMethod = ctx.request().method().name();

        try {
            logger.debug("Request[" + ctx.request().streamId() + "] (" + ctx.request() + ") begins");
            PathParsedHandlerClassMethod result = parsePathToHandler(requestMethod, requestPath);
            logger.debug("Request[" + ctx.request().streamId() + "] target handler: " + result.handlerClass + "::" + result.method, new JsonObject().put("parameters", result.parameters));
            run(ctx, result);
        } catch (NoSuchMethodException e) {
            getLogger().exception("Request[" + ctx.request().streamId() + "]", e);
            ctx.response().setStatusCode(404).end("This request could not be handled by proper handler!");
        }
    }

    private PathParsedHandlerClassMethod parsePathToHandler(String requestMethod, String requestPath) throws NoSuchMethodException {
//        getLogger().info("requestPath: " + requestPath);
        // here, the `requestPath` should not be empty or '/'
//        System.out.println("! requestPath=" + requestPath + " this.pathPrefix=" + this.pathPrefix);
        String requestPathWithoutPrefix = requestPath.substring(this.pathPrefix.length());
        //System.out.println("parsePathToHandler from "+requestPath+" to "+requestPathWithoutPrefix);
        String[] pathComponents = requestPathWithoutPrefix.split("/");
        StringBuilder className = new StringBuilder(controllerPackage);

        for (int i = 0; i < pathComponents.length - 1; i++) {
            String pathComponent = pathComponents[i];

            if (pathComponent == null || pathComponent.trim().equals("")) {
                continue;
            }
            // current className + pathComponent is class ?
            try {
                String testClassName = className + "." + pathComponent;
                getLogger().info("testClassName: " + testClassName);
                Class<?> handlerClass = Class.forName(testClassName);
                Method[] methods = handlerClass.getMethods();
                for (Method method : methods) {
                    if (!isMethodPublicNonStaticNotInheritedAndReturnFuture(method)) continue;

                    KeelApiAnnotation annotation = getKeelApiAnnotationForMethod(method);
                    if (annotation.acceptedRequestMethods().length > 0) {
                        boolean methodNotSupported = true;
                        for (var rm : annotation.acceptedRequestMethods()) {
                            if (rm.equalsIgnoreCase(requestMethod)) {
                                methodNotSupported = false;
                                break;
                            }
                        }
                        if (methodNotSupported) {
                            continue;
                        }
                    }

                    getLogger().info(
                            "API " + method.getClass() + "::" + method.getName(),
                            new JsonObject()
                                    .put("parameters", method.getParameterCount())
                                    .put("return", method.getReturnType().toString())
                                    .put("KeelApiAnnotation", new JsonObject()
                                            .put("acceptedRequestMethods", Arrays.asList(annotation.acceptedRequestMethods()))
                                            .put("responseContentType", annotation.responseContentType())
                                    )
                    );

                    if (
                            method.getName().equals(pathComponents[i + 1])
                                    && method.getParameterCount() == pathComponents.length - i - 2
                    ) {
                        // YES!
                        PathParsedHandlerClassMethod pathParsedHandlerClassMethod = new PathParsedHandlerClassMethod(handlerClass, method);
                        for (int j = i + 2; j < pathComponents.length; j++) {
                            pathParsedHandlerClassMethod.addParameter(pathComponents[j]);
                        }
                        return pathParsedHandlerClassMethod;
                    }
                }
            } catch (ClassNotFoundException e) {
                getLogger().warning("ClassNotFoundException: " + e.getMessage());
                // seek next
                className.append(".").append(pathComponent);
            }
        }
        throw new NoSuchMethodException();
    }

    private void run(RoutingContext ctx, PathParsedHandlerClassMethod handlerClassMethod) {
        KeelApiAnnotation annotation = getKeelApiAnnotationForMethod(handlerClassMethod.method);

        filterForRequestedMethod(ctx, handlerClassMethod.method, filterClassList)
                .compose(filtered -> {
                    try {
                        KeelWebRequestController controller = (KeelWebRequestController) handlerClassMethod.handlerClass.getDeclaredConstructor(RoutingContext.class).newInstance(ctx);
                        return (Future<?>) handlerClassMethod.method.invoke(controller, handlerClassMethod.parameters.toArray());
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        return Future.failedFuture(e);
                    }
                })
                .compose(result -> {
                    if (result == null || annotation.directlyOutput()) {
                        return Future.succeededFuture();
                    }
                    return respond(annotation, ctx, result);
                })
                .onFailure(throwable -> {
                    getLogger().exception("CONTROLLER EXECUTE THROW", throwable);
                    respond(annotation, ctx, throwable);
                });
    }

    private static class PathParsedHandlerClassMethod {
        public final Class<?> handlerClass;
        public final Method method;
        public final ArrayList<String> parameters;

        public PathParsedHandlerClassMethod(Class<?> handlerClass, Method method) {
            this.handlerClass = handlerClass;
            this.method = method;
            this.parameters = new ArrayList<>();
        }

        public void addParameter(String parameter) {
            this.parameters.add(parameter);
        }
    }
}
