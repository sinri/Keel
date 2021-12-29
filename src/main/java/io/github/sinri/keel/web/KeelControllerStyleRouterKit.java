package io.github.sinri.keel.web;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class KeelControllerStyleRouterKit {
    private final String controllerPackage;
    private final List<KeelWebRequestFilter> filterList = new ArrayList<>();

    public KeelControllerStyleRouterKit(String controllerPackage) {
        // such as "com.leqee.oc.tachiba.handler"
        this.controllerPackage = controllerPackage;
    }

    /**
     * @param controllerPackage such as "com.leqee.oc.tachiba.handler"
     * @param filterList        list of filters
     * @since 1.1
     */
    public KeelControllerStyleRouterKit(String controllerPackage, List<KeelWebRequestFilter> filterList) {
        this.controllerPackage = controllerPackage;
        this.filterList.addAll(filterList);
    }

    public KeelControllerStyleRouterKit addFilter(KeelWebRequestFilter filter) {
        this.filterList.add(filter);
        return this;
    }

    public void processRouterRequest(RoutingContext ctx) {
        String requestPath = ctx.request().path();

        try {
            PathParsedHandlerClassMethod result = parsePathToHandler(requestPath);
            result.run(ctx, filterList);
        } catch (NoSuchMethodException e) {
            System.out.println(
                    "UNEXPECTED REQUEST: "
                            + ctx.request().method().toNetty() + " " + ctx.request().uri()
                            + " CAUSED BY " + e.getClass() + " : " + e.getCause()
            );
            ctx.response().setStatusCode(404).end("This request could not be handled by proper handler!");
        }
    }

    private PathParsedHandlerClassMethod parsePathToHandler(String requestPath) throws NoSuchMethodException {
//        TachibaWebService.getLogger().info("requestPath: " + requestPath);
        // here, the `requestPath` should not be empty or '/'
        String[] pathComponents = requestPath.split("/");
        StringBuilder className = new StringBuilder(controllerPackage);

        for (int i = 0; i < pathComponents.length - 1; i++) {
            String pathComponent = pathComponents[i];

            if (pathComponent == null || pathComponent.trim().equals("")) {
                continue;
            }
            // current className + pathComponent is class ?
            try {
                String testClassName = className + "." + pathComponent;
                System.out.println("testClassName: " + testClassName);
                Class<?> handlerClass = Class.forName(testClassName);
                Method[] methods = handlerClass.getMethods();
                for (Method method : methods) {
                    System.out.println("Method: " + method.getName() + " with " + method.getParameterCount());
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
//                TachibaWebService.getLogger().warning("ClassNotFoundException: " + e.getMessage());
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

        public PathParsedHandlerClassMethod(Class<?> handlerClass, Method method) {
            this.handlerClass = handlerClass;
            this.method = method;
            this.parameters = new ArrayList<>();
        }

        public void addParameter(String parameter) {
            this.parameters.add(parameter);
        }

        public void run(RoutingContext ctx, List<KeelWebRequestFilter> filterList) {
            Future<Void> filterFuture = new KeelWebRequestFilter().shouldHandleThisRequest(ctx);
            for (var filter : filterList) {
                filterFuture = filterFuture.compose(x -> filter.shouldHandleThisRequest(ctx));
            }
            filterFuture.onFailure(
                            throwable -> ctx.response().setStatusCode(403).end(throwable.getMessage())
                    )
                    .compose(x -> {
                        try {
                            // now do not check if it is an extension of TachibaRequestHandler
                            try {
                                KeelWebRequestController controller = (KeelWebRequestController) handlerClass.getDeclaredConstructor(RoutingContext.class).newInstance(ctx);
                                method.invoke(controller, this.parameters.toArray());
                            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                                ctx.response().setStatusCode(404).end(e.getMessage());
                            }
                        } catch (Exception anyError) {
                            ctx.response().setStatusCode(500).end(anyError.getMessage());
                        }
                        return Future.succeededFuture();
                    });
        }
    }

}
