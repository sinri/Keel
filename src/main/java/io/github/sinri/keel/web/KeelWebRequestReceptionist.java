package io.github.sinri.keel.web;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.controlflow.FutureForEach;
import io.github.sinri.keel.core.json.JsonifiableEntity;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.verticles.KeelVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @since 2.0
 */
abstract public class KeelWebRequestReceptionist extends KeelVerticle {
    public static final String RoutingContextDatumKeyOfClientIPChain = "client_ip_chain";
    public static final String RoutingContextDatumKeyOfRequestID = "request_id";
    private final RoutingContext routingContext;
    private String requestID;

    public KeelWebRequestReceptionist(RoutingContext routingContext) {
        this.routingContext = routingContext;
        this.requestID = new Date().getTime() + "#UNDEPLOYED";
    }

    public static void registerRoute(
            Route route,
            Class<? extends KeelWebRequestReceptionist> receptionistClass,
            boolean withBodyHandler,
            KeelLogger logger
    ) {
        if (withBodyHandler) {
            route = route.handler(BodyHandler.create());
        }
        route.handler(ctx -> {
            try {
                receptionistClass.getConstructor(RoutingContext.class)
                        .newInstance(ctx)
                        .deployMe()
                        .compose(deploymentID -> {
                            logger.debug("receptionistClass " + receptionistClass.getName() + " deployed as " + deploymentID);
                            return Future.succeededFuture();
                        });
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                logger.exception("receptionistClass " + receptionistClass.getName() + " cannot be deployed", e);
                ctx.fail(404);
            }
        });
    }

    public static void registerApiServiceClass(Router router, Class<? extends KeelWebRequestReceptionist> c, KeelLogger logger) {
        ApiMeta annotation = c.getAnnotation(ApiMeta.class);
        if (annotation == null) {
            logger.warning("class " + c.getName() + " without ApiMeta, passover");
            return;
        }

        Route route = router.route(annotation.routePath());
        String[] methods = annotation.allowMethods();
        if (methods != null) {
            for (var methodName : methods) {
                HttpMethod hm = HttpMethod.valueOf(methodName);
                route.method(hm);
            }
        }
        if (!annotation.virtualHost().equals("")) {
            route.virtualHost(annotation.virtualHost());
        }

        registerRoute(route, c, annotation.requestBodyNeeded(), logger);
    }

    public static void registerApiServicesInPackage(Router router, String packageName, KeelLogger logger) {
        Reflections reflections = new Reflections(packageName);

        Set<Class<? extends KeelWebRequestReceptionist>> allClasses = reflections.getSubTypesOf(KeelWebRequestReceptionist.class);
        allClasses.forEach(c -> registerApiServiceClass(router, c, logger));
    }

    protected String prepareRequestID() {
        return new Date().getTime() + "#" + deploymentID();
    }

    protected final String getRequestID() {
        return requestID;
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

    /**
     * @since 2.4
     */
    private void parseClientIPChain() {
        // X-Forwarded-For
        JsonArray clientIPChain = new JsonArray();
        String xForwardedFor = getRoutingContext().request().getHeader("X-Forwarded-For");
        if (xForwardedFor != null) {
            String[] split = xForwardedFor.split("[ ,]+");
            for (var item : split) {
                clientIPChain.add(item);
            }
        }
        clientIPChain.add(getRoutingContext().request().remoteAddress().hostAddress());

        getRoutingContext().put(RoutingContextDatumKeyOfClientIPChain, clientIPChain);
    }

    @Override
    public void start() throws Exception {
        super.start();

        Keel.registerDeployedKeelVerticle(this);

        this.requestID = prepareRequestID();
        this.getRoutingContext().put(RoutingContextDatumKeyOfRequestID, this.requestID);

        setLogger(prepareLogger());

        // check method
        if (getAcceptableMethod() != null && !getAcceptableMethod().isEmpty()) {
            if (!getAcceptableMethod().contains(this.getRoutingContext().request().method().name())) {
                getLogger().warning("method: " + this.getRoutingContext().request().method().name() + " -> 405");
                getRoutingContext().fail(405);
                return;
            }
        }

        // client ip chain
        parseClientIPChain();

        // filters
        dealWithFilters()
                .compose(filtersPassed -> {
                    return this.handlerForFiltersPassed();
                })
                .compose(responseObject -> {
                    getLogger().debug("RECEPTIONIST DONE FOR " + deploymentID());

                    return respond(responseObject);
                })
                .recover(throwable -> {
                    getLogger().exception("RECEPTIONIST FAILED FOR " + deploymentID(), throwable);

                    return respond(throwable);
                })
                .eventually(v -> {
                    // @since 2.2 undeploy me
                    getLogger().debug("RECEPTIONIST eventually undeploy me!");
                    return undeployMe();
                });
    }

    protected Future<Void> dealWithFilters() {
        return FutureForEach.call(
                this.getFilterClassList(),
                filterClass -> {
                    KeelWebRequestFilter filter;
                    try {
                        filter = filterClass.getConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                             NoSuchMethodException e) {
                        return Future.failedFuture(e);
                    }
                    return filter.shouldHandleThisRequest(getRoutingContext());
                }
        );
    }

    protected Future<Object> handlerForFiltersPassed() {
        return this.dealWithRequest();
    }

    abstract protected Future<Object> dealWithRequest();

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
            getLogger().debug("Response for request [" + getRequestID() + "] as json", jsonObject);
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
            getLogger().debug(
                    "Response for request [" + getRequestID() + "] as " + this.getResponseHeaderContentType(),
                    new JsonObject()
                            .put("code", code)
                            .put("data", data)
            );
            return this.getRoutingContext().response().setStatusCode(code).end(data);
        }
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Keel.unregisterDeployedKeelVerticle(this.deploymentID());
    }
}
