package io.github.sinri.keel.web;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureRecursion;
import io.github.sinri.keel.core.json.JsonifiableEntity;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @param <R>
 * @since 2.0
 */
abstract public class KeelWebRequestReceptionist<R> extends KeelVerticle {
    private String requestID;
    private final RoutingContext routingContext;

    public KeelWebRequestReceptionist(RoutingContext routingContext) {
        this.routingContext = routingContext;
        this.requestID = new Date().getTime() + "#UNDEPLOYED";
    }

    protected String prepareRequestID() {
        return new Date().getTime() + "#" + deploymentID();
    }

    protected final String getRequestID() {
        return requestID;
    }

    public static <T> void registerRoute(
            Route route,
            Class<? extends KeelWebRequestReceptionist<T>> receptionistClass,
            boolean withBodyHandler
    ) {
        if (withBodyHandler) {
            route = route.handler(BodyHandler.create());
        }
        KeelLogger outputLogger = Keel.outputLogger("KeelWebRequestReceptionist::registerRoute");
        route.handler(ctx -> {
            try {
                receptionistClass.getConstructor(RoutingContext.class)
                        .newInstance(ctx)
                        .deployMe()
                        .compose(deploymentID -> {
                            outputLogger.debug("receptionistClass " + receptionistClass.getName() + " deployed as " + deploymentID);
                            return Future.succeededFuture();
                        });
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                outputLogger.exception(e);
                ctx.fail(404);
            }
        });
    }

    abstract protected KeelLogger prepareLogger();

    public final RoutingContext getRoutingContext() {
        return routingContext;
    }

    protected List<Class<? extends KeelWebRequestFilter>> getFilterClassList() {
        return new ArrayList<>();
    }

    protected String getResponseHeaderContentType() {
        return "application/json";
    }

    protected Set<String> getAcceptableMethod() {
        return new HashSet<>();
    }

    @Override
    public void start() throws Exception {
        super.start();
        this.requestID = prepareRequestID();
        setKeelLogger(prepareLogger());

        // check method
        if (getAcceptableMethod() != null && !getAcceptableMethod().isEmpty()) {
            if (!getAcceptableMethod().contains(this.getRoutingContext().request().method().name())) {
                getLoggerInContext().warning("method: " + this.getRoutingContext().request().method().name() + " -> 405");
                getRoutingContext().fail(405);
                return;
            }
        }
        // filters
        List<Class<? extends KeelWebRequestFilter>> filterClassList = this.getFilterClassList();
        new FutureRecursion<Integer>(
                filterIndex -> Future.succeededFuture(filterIndex < filterClassList.size()),
                filterIndex -> {
                    Class<? extends KeelWebRequestFilter> filterClass = filterClassList.get(filterIndex);
                    KeelWebRequestFilter filter;
                    try {
                        filter = filterClass.getConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        return Future.failedFuture(e);
                    }
                    return filter.shouldHandleThisRequest(getRoutingContext())
                            .compose(ok -> Future.succeededFuture(filterIndex + 1));
                }
        )
                .run(0)
                .compose(x -> Future.succeededFuture())
                .compose(filtersPassed -> this.dealWithRequest())
                .compose(responseObject -> {
                    getLoggerInContext().notice("RECEPTIONIST DONE FOR " + deploymentID());

                    return respond(responseObject);
                })
                .recover(throwable -> {
                    getLoggerInContext().exception("RECEPTIONIST FAILED FOR " + deploymentID(), throwable);

                    return respond(throwable);
                })
                .eventually(v -> {
                    getLoggerInContext().notice("eventually");
                    return Future.succeededFuture();
                });
    }

    abstract protected Future<R> dealWithRequest();

    // abstract public Route registerRoute(Router router);

    protected Future<Void> respond(Object result) {
        String responseHeaderContentType = this.getResponseHeaderContentType();
        if (
                responseHeaderContentType.equalsIgnoreCase("application/json")
                        || responseHeaderContentType.startsWith("application/json;")
        ) {
            // output as JSON: all 200, while JSON field `code` shows OK or FAILED
            JsonObject jsonObject = new JsonObject()
                    .put("__debug__", new JsonObject()
                            .put("request_id", getRequestID())
                    );
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

                jsonObject.put("code", "FAILED").put("data", sb.toString());
            } else if (result instanceof JsonifiableEntity) {
                jsonObject.put("code", "OK").put("data", ((JsonifiableEntity<?>) result).toJsonObject());
            } else {
                jsonObject.put("code", "OK").put("data", result);
            }
            getLoggerInContext().debug("Response for request [" + getRequestID() + "] as json", jsonObject);
            return this.getRoutingContext().json(jsonObject);
        } else {
            // NON JSON, AS certain MIME string to output, 500 for ERROR
            this.getRoutingContext().response().putHeader("Content-Type", this.getResponseHeaderContentType());
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
                data = "RequestID [" + getRequestID() + "] ERROR: " + sb;
            } else {
                data = result.toString();
            }
            getLoggerInContext().debug(
                    "Response for request [" + getRequestID() + "] as " + this.getResponseHeaderContentType(),
                    new JsonObject()
                            .put("code", code)
                            .put("data", data)
            );
            return this.getRoutingContext().response().setStatusCode(code).end(data);
        }
    }
}
