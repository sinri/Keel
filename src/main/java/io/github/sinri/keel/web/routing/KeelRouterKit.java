package io.github.sinri.keel.web.routing;

import io.github.sinri.keel.core.JsonifiableEntity;
import io.github.sinri.keel.core.controlflow.FutureForEach;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.web.KeelApiAnnotation;
import io.github.sinri.keel.web.KeelWebRequestController;
import io.github.sinri.keel.web.KeelWebRequestFilter;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

abstract public class KeelRouterKit {
    protected KeelLogger logger = KeelLogger.buildSilentLogger();

    protected Future<Void> checkMethodAcceptable(KeelApiAnnotation annotation, RoutingContext ctx) {
        boolean should405 = true;
        String[] acceptedRequestMethods = annotation.acceptedRequestMethods();
        if (acceptedRequestMethods.length > 0) {
            for (var acceptedMethod : acceptedRequestMethods) {
                if (ctx.request().method().name().equalsIgnoreCase(acceptedMethod)) {
                    should405 = false;
                    break;
                }
            }
            if (should405) {
                return Future.failedFuture("Method is not allowed");
            }
        }
        return Future.succeededFuture();
    }

    public static Future<Void> respond(KeelLogger logger, KeelApiAnnotation annotation, RoutingContext ctx, Object result) {
        if (
                annotation.responseContentType().equalsIgnoreCase("application/json")
                        || annotation.responseContentType().startsWith("application/json;")
        ) {
            // output as JSON: all 200, while JSON field `code` shows OK or FAILED
            JsonObject jsonObject = new JsonObject();
            if (result == null) {
                jsonObject.put("code", "OK").putNull("data");
            } else if (result instanceof Throwable) {
                StringBuilder sb = new StringBuilder();
                var t = (Throwable) result;
                while (t != null) {
                    if (sb.length() > 0) {
                        sb.append(" ← ");
                    }
                    sb.append(t.getClass().getName()).append(" : ").append(t.getMessage());
                    t = t.getCause();
                }

                jsonObject.put("code", "FAILED")
                        .put("data", sb.toString());
            } else if (result instanceof JsonifiableEntity) {
                jsonObject.put("code", "OK").put("data", ((JsonifiableEntity<?>) result).toJsonObject());
            } else {
                jsonObject.put("code", "OK").put("data", result);
            }
            logger.debug("Response for request [" + ctx.request().streamId() + "] as json", jsonObject);
            return ctx.json(jsonObject);
        } else {
            // NON JSON, AS certain MIME string to output, 500 for ERROR
            ctx.response().putHeader("Content-Type", annotation.responseContentType());
            int code = 200;
            String data;
            if (result == null) {
                data = "";
            } else if (result instanceof Throwable) {
                StringBuilder sb = new StringBuilder();
                var t = (Throwable) result;
                while (t != null) {
                    if (sb.length() > 0) {
                        sb.append(" ← ");
                    }
                    sb.append(t.getClass().getName()).append(" : ").append(t.getMessage());
                    t = t.getCause();
                }

                code = 500;
                data = "ERROR: " + sb;
            } else {
                data = result.toString();
            }
            logger.debug(
                    "Response for request [" + ctx.request().streamId() + "] as " + annotation.responseContentType(),
                    new JsonObject()
                            .put("code", code)
                            .put("data", data)
            );
            return ctx.response().setStatusCode(code).end(data);
        }
    }

    protected Future<Void> respond(KeelApiAnnotation annotation, RoutingContext ctx, Object result) {
        return respond(logger, annotation, ctx, result);
    }

    protected boolean isMethodPublicNonStaticNotInheritedAndReturnFuture(Method method) {
        return (
                Modifier.isPublic(method.getModifiers())
                        && !Modifier.isStatic(method.getModifiers())
                        && KeelWebRequestController.class.isAssignableFrom(method.getDeclaringClass())
                        && Future.class.isAssignableFrom(method.getReturnType())
        );
    }

    protected Future<Void> filterForRequestedMethod(RoutingContext ctx, Method method, List<Class<? extends KeelWebRequestFilter>> filterClassList) {
        return FutureForEach.quick(
                filterClassList,
                filterClass -> {
                    KeelWebRequestFilter filter;
                    try {
                        filter = filterClass.getConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        logger.exception(
                                "filter [" + filterClass.getName() + "] error for Requested Method [" + method.getName() + "] of class [" + method.getDeclaringClass().getName() + "]",
                                e
                        );
                        return Future.failedFuture(e);
                    }
                    filter.setTargetMethod(method);
                    return filter.shouldHandleThisRequest(ctx);
                }
        );
    }
}
