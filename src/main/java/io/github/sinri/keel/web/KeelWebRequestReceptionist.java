package io.github.sinri.keel.web;

import io.github.sinri.keel.core.JsonifiableEntity;
import io.github.sinri.keel.core.controlflow.FutureRecursion;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.RoutingContext;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @param <R>
 * @since 2.0
 */
abstract public class KeelWebRequestReceptionist<R> extends KeelVerticle {
    private final RoutingContext routingContext;
    private KeelLogger logger;

    public KeelWebRequestReceptionist(RoutingContext routingContext) {
        this.routingContext = routingContext;
        this.logger = KeelLogger.buildSilentLogger();
    }

    public static <T> Route registerRoute(Route route, Class<? extends KeelWebRequestReceptionist<T>> receptionistClass) {
        return route.handler(ctx -> {
            try {
                receptionistClass.getConstructor(RoutingContext.class)
                        .newInstance(ctx)
                        .deployMe();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                ctx.fail(404);
            }
        });
    }

    public KeelLogger getLogger() {
        return logger;
    }

    protected KeelWebRequestReceptionist<R> setLogger(KeelLogger logger) {
        this.logger = logger;
        return this;
    }

    public RoutingContext getRoutingContext() {
        return routingContext;
    }

    protected List<Class<? extends KeelWebRequestFilter>> getFilterClassList() {
        return new ArrayList<>();
    }

    protected String getResponseHeaderContentType() {
        return "application/json";
    }

    protected Set<HttpMethod> getAcceptableMethod() {
        var set = new HashSet<HttpMethod>();
        set.add(HttpMethod.GET);
        set.add(HttpMethod.POST);
        set.add(HttpMethod.HEAD);
        set.add(HttpMethod.OPTIONS);
        return set;
    }

    @Override
    public void start() throws Exception {
        super.start();
        // check method
        if (!getAcceptableMethod().contains(this.getRoutingContext().request().method())) {
            getRoutingContext().fail(405);
            return;
        }
        // filters
        List<Class<? extends KeelWebRequestFilter>> filterClassList = this.getFilterClassList();
        new FutureRecursion<Integer>(
                filterIndex -> {
                    return Future.succeededFuture(filterIndex < filterClassList.size());
                },
                filterIndex -> {
                    Class<? extends KeelWebRequestFilter> filterClass = filterClassList.get(filterIndex);
                    KeelWebRequestFilter filter = null;
                    try {
                        filter = filterClass.getConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        return Future.failedFuture(e);
                    }
                    return filter.shouldHandleThisRequest(getRoutingContext())
                            .compose(ok -> {
                                return Future.succeededFuture(filterIndex + 1);
                            });
                }
        )
                .run(0)
                .onComplete(filteredResult -> {
                    if (filteredResult.failed()) {
                        this.getRoutingContext().fail(400);
                        return;
                    }

                    // make response
                    this.dealWithRequest()
                            .onComplete(asyncResult -> {
                                Object result;
                                if (asyncResult.succeeded()) {
                                    result = asyncResult.result();
                                } else {
                                    result = asyncResult.cause();
                                }
                                respond(result)
                                        .compose(over -> {
                                            // stop and undeploy!
                                            return this.undeployMe();
                                        });
                            });
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
            logger.debug("Response for request [" + this.getRoutingContext().request().streamId() + "] as json", jsonObject);
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
                data = "ERROR: " + sb;
            } else {
                data = result.toString();
            }
            logger.debug(
                    "Response for request [" + this.getRoutingContext().request().streamId() + "] as " + this.getResponseHeaderContentType(),
                    new JsonObject()
                            .put("code", code)
                            .put("data", data)
            );
            return this.getRoutingContext().response().setStatusCode(code).end(data);
        }
    }
}
