package io.github.sinri.Keel.web;

import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class KeelControllerStyleRouterKit {
    private final String controllerPackage;

    public KeelControllerStyleRouterKit(String controllerPackage) {
        // such as "com.leqee.oc.tachiba.handler"
        this.controllerPackage = controllerPackage;
    }

    public void processRouterRequest(RoutingContext ctx) {
        String requestPath = ctx.request().path();

        try {
            PathParsedHandlerClassMethod result = parsePathToHandler(requestPath);
            result.run(ctx);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
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
        public Class<?> handlerClass;
        public Method method;
        public ArrayList<String> parameters;

        public PathParsedHandlerClassMethod(Class<?> handlerClass, Method method) {
            this.handlerClass = handlerClass;
            this.method = method;
            this.parameters = new ArrayList<>();
        }

        public void addParameter(String parameter) {
            this.parameters.add(parameter);
        }

        public void run(RoutingContext ctx) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
            // now do not check if it is an extension of TachibaRequestHandler
            KeelWebRequestController controller = (KeelWebRequestController) handlerClass.getDeclaredConstructor(RoutingContext.class).newInstance(ctx);
            method.invoke(controller, this.parameters.toArray());
        }
    }

}
