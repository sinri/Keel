package io.github.sinri.keel.web;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KeelControllerStyleRouterKit {
    private final String pathPrefix;
    private final String controllerPackage;
    private final List<KeelWebRequestFilter> filterList = new ArrayList<>();
    private KeelLogger logger = KeelLogger.buildSilentLogger();

    public KeelControllerStyleRouterKit(String controllerPackage) {
        // such as "com.leqee.oc.tachiba.handler"
        this.controllerPackage = controllerPackage;
        this.pathPrefix = "";
    }

    /**
     * @param controllerPackage such as "com.leqee.oc.tachiba.handler"
     * @param filterList        list of filters
     * @since 1.1
     */
    public KeelControllerStyleRouterKit(String controllerPackage, List<KeelWebRequestFilter> filterList) {
        this.controllerPackage = controllerPackage;
        this.filterList.addAll(filterList);
        this.pathPrefix = "";
    }

    /**
     * @param pathPrefix        such as '/api/'
     * @param controllerPackage such as "com.leqee.oc.tachiba.handler"
     * @param filterList        list of filters
     * @since 1.10
     */
    public KeelControllerStyleRouterKit(String pathPrefix, String controllerPackage, List<KeelWebRequestFilter> filterList) {
        this.controllerPackage = controllerPackage;
        this.filterList.addAll(filterList);
        this.pathPrefix = pathPrefix;
    }

    public static KeelApiAnnotation getKeelApiAnnotationForMethod(Method method) {
        KeelApiAnnotation annotation = method.getAnnotation(KeelApiAnnotation.class);
        if (annotation == null) {
            annotation = new KeelApiAnnotation() {
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
            };
        }
        return annotation;
    }

    public KeelLogger getLogger() {
        return logger;
    }

    public void setLogger(KeelLogger logger) {
        this.logger = logger;
    }

    public KeelControllerStyleRouterKit addFilter(KeelWebRequestFilter filter) {
        this.filterList.add(filter);
        return this;
    }

    public void processRouterRequest(RoutingContext ctx) {
        String requestPath = ctx.request().path();
        String requestMethod = ctx.request().method().name();

        try {
            PathParsedHandlerClassMethod result = parsePathToHandler(requestMethod, requestPath);
            result.run(ctx, filterList);
        } catch (NoSuchMethodException e) {
            getLogger().warning(
                    "UNEXPECTED REQUEST: "
                            + ctx.request().method().toNetty() + " " + ctx.request().uri()
                            + " CAUSED BY " + e.getClass() + " : " + e.getCause()
            );
            ctx.response().setStatusCode(404).end("This request could not be handled by proper handler!");
        }
    }

    private PathParsedHandlerClassMethod parsePathToHandler(String requestMethod, String requestPath) throws NoSuchMethodException {
//        getLogger().info("requestPath: " + requestPath);
        // here, the `requestPath` should not be empty or '/'
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
                    if (!Modifier.isPublic(method.getModifiers())) {
                        continue;
                    }
                    if (Modifier.isStatic(method.getModifiers())) {
                        continue;
                    }
                    if (!KeelWebRequestController.class.isAssignableFrom(method.getDeclaringClass())) {
                        continue;
                    }

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
                        pathParsedHandlerClassMethod.setLogger(getLogger());
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

    private static class PathParsedHandlerClassMethod {
        public final Class<?> handlerClass;
        public final Method method;
        public final ArrayList<String> parameters;
        private KeelLogger logger;

        public PathParsedHandlerClassMethod(Class<?> handlerClass, Method method) {
            this.handlerClass = handlerClass;
            this.method = method;
            this.parameters = new ArrayList<>();
        }

        public KeelLogger getLogger() {
            return logger;
        }

        public void setLogger(KeelLogger logger) {
            this.logger = logger;
        }

        public void addParameter(String parameter) {
            this.parameters.add(parameter);
        }

        public void run(RoutingContext ctx, List<KeelWebRequestFilter> filterList) {
            KeelApiAnnotation annotation = getKeelApiAnnotationForMethod(method);

            Future<Void> filterFuture = Future.succeededFuture();
            for (var filter : filterList) {
                filterFuture = filterFuture.compose(x -> filter.shouldHandleThisRequest(ctx));
            }
            filterFuture
                    .onFailure(throwable -> ctx.response().setStatusCode(403).end("Thrown by filter: " + throwable.getMessage()))
                    .onSuccess(x -> {
                        try {
                            // now do not check if it is an extension of TachibaRequestHandler
                            try {
                                KeelWebRequestController controller = (KeelWebRequestController) handlerClass.getDeclaredConstructor(RoutingContext.class).newInstance(ctx);

                                Object invoked = null;
                                try {
//                                    getLogger().info("GO invoke");
                                    invoked = method.invoke(controller, this.parameters.toArray());
//                                    getLogger().info("WENT invoke");
                                } catch (InvocationTargetException invocationTargetException) {
                                    getLogger().error("method invoked but threw Exception from it, so InvocationTargetException occurred");
                                    getLogger().exception(invocationTargetException);
                                    Throwable targetException = invocationTargetException.getTargetException();
                                    if (targetException != null) {
                                        controller.sayFail("Met InvocationTargetException with Cause: " + targetException.getMessage());
                                    } else {
                                        controller.sayFail("Met InvocationTargetException without Cause");
                                    }
                                }
                                if (invoked != null) {
                                    if (invoked instanceof Future) {
                                        ((Future<?>) invoked)
                                                .onSuccess(result -> {
                                                    if (
                                                            annotation.responseContentType().equals("application/json")
                                                                    || annotation.responseContentType().startsWith("application/json;")
                                                    ) {
                                                        controller.sayOK(result);
                                                    } else {
                                                        ctx.response()
                                                                .setStatusCode(200)
                                                                .putHeader("Content-Type", annotation.responseContentType())
                                                                .end(String.valueOf(result));
                                                    }
                                                })
                                                .onFailure(throwable -> {
                                                    getLogger().exception(throwable);
                                                    if (
                                                            annotation.responseContentType().equals("application/json")
                                                                    || annotation.responseContentType().startsWith("application/json;")
                                                    ) {
                                                        controller.sayFail(throwable.getClass().getName() + ": " + throwable.getMessage());
                                                    } else {
                                                        ctx.response()
                                                                .setStatusCode(200)
                                                                .putHeader("Content-Type", annotation.responseContentType())
                                                                .end(throwable.getClass().getName() + ": " + throwable.getMessage());
                                                    }
                                                });
                                    } else {
                                        controller.sayOK(invoked);
                                    }
                                }
                            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                                getLogger().exception(e);
                                ctx.response().setStatusCode(404).end(e.getClass().getName() + " " + e.getMessage());
                            }
                        } catch (Exception anyError) {
                            getLogger().exception(anyError);
                            ctx.response().setStatusCode(500).end(anyError.getClass().getName() + " " + anyError.getMessage());
                        }
                    });
        }
    }

}
