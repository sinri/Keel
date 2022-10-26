package io.github.sinri.keel.web.service;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.web.ApiMeta;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.*;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @param <S>
 * @since 2.9
 */
public class KeelWebRequestRouteKit<S extends KeelWebRequestHandler> {
    private final Router router;
    private final Class<S> classOfService;
    private final List<PlatformHandler> platformHandlers = new ArrayList<>();
    private final List<SecurityPolicyHandler> securityPolicyHandlers = new ArrayList<>();
    private final List<ProtocolUpgradeHandler> protocolUpgradeHandlers = new ArrayList<>();
    private final List<MultiTenantHandler> multiTenantHandlers = new ArrayList<>();
    private final List<AuthenticationHandler> authenticationHandlers = new ArrayList<>();
    private final List<InputTrustHandler> inputTrustHandlers = new ArrayList<>();
    private final List<AuthorizationHandler> authorizationHandlers = new ArrayList<>();
    private final List<Handler<RoutingContext>> userHandlers = new ArrayList<>();
    private String uploadDirectory = BodyHandler.DEFAULT_UPLOADS_DIRECTORY;
    private String virtualHost = null;
    /**
     * Cross Origin Resource Sharing
     * If null: use ApiMate Definition
     * If "": do not allow CORS;
     * If "*": allow all (*)
     * Else: as a Regex Pattern (DOMAIN)
     */
    private String corsOriginPattern = null;

    public KeelWebRequestRouteKit(Class<S> classOfService) {
        this.classOfService = classOfService;
        router = Router.router(Keel.getVertx());
    }

    public KeelWebRequestRouteKit(Class<S> classOfService, Router router) {
        this.classOfService = classOfService;
        this.router = router;
    }

    public KeelWebRequestRouteKit<S> addPlatformHandler(PlatformHandler handler) {
        this.platformHandlers.add(handler);
        return this;
    }

    public KeelWebRequestRouteKit<S> addSecurityPolicyHandler(SecurityPolicyHandler handler) {
        this.securityPolicyHandlers.add(handler);
        return this;
    }

    public KeelWebRequestRouteKit<S> addProtocolUpgradeHandler(ProtocolUpgradeHandler handler) {
        this.protocolUpgradeHandlers.add(handler);
        return this;
    }

    public KeelWebRequestRouteKit<S> addMultiTenantHandler(MultiTenantHandler handler) {
        this.multiTenantHandlers.add(handler);
        return this;
    }

    /**
     * 追加一个认证校验器
     */
    public KeelWebRequestRouteKit<S> addAuthenticationHandler(AuthenticationHandler handler) {
        this.authenticationHandlers.add(handler);
        return this;
    }

    public KeelWebRequestRouteKit<S> addInputTrustHandler(InputTrustHandler handler) {
        this.inputTrustHandlers.add(handler);
        return this;
    }

    /**
     * 追加一个授权校验器
     */
    public KeelWebRequestRouteKit<S> addAuthorizationHandlers(AuthorizationHandler handler) {
        this.authorizationHandlers.add(handler);
        return this;
    }

    public KeelWebRequestRouteKit<S> addUserHandler(Handler<RoutingContext> handler) {
        this.userHandlers.add(handler);
        return this;
    }

    public KeelWebRequestRouteKit<S> setUploadDirectory(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
        return this;
    }

    /**
     * @since 2.9
     */
    public KeelWebRequestRouteKit<S> setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
        return this;
    }

    public KeelWebRequestRouteKit<S> setCorsOriginPattern(String corsOriginPattern) {
        this.corsOriginPattern = corsOriginPattern;
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

        try {
            allClasses.forEach(this::loadClass);
        } catch (Exception e) {
            Keel.outputLogger().exception("KeelWebRequestRouteKit::loadPackage THROWS", e);
        }

        return this;
    }

    /**
     * @since 2.9
     */
    public KeelWebRequestRouteKit<S> loadClass(Class<? extends S> c) {
        ApiMeta apiMeta = Keel.helpers().reflection().getAnnotationOfClass(c, ApiMeta.class);
        if (apiMeta == null) return this;

        Keel.outputLogger().debug("KeelWebRequestRouteKit Loading " + c.getName());

        S handler;
        try {
            handler = c.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            Keel.outputLogger().exception("HANDLER REFLECTION EXCEPTION", e);
            return this;
        }

        Route route = router.route(apiMeta.routePath());

        if (apiMeta.allowMethods() != null) {
            for (var methodName : apiMeta.allowMethods()) {
                route.method(HttpMethod.valueOf(methodName));
            }
        }

        if (apiMeta.virtualHost() != null && !apiMeta.virtualHost().equals("")) {
            route.virtualHost(apiMeta.virtualHost());
        } else if (this.virtualHost != null && !Objects.equals("", this.virtualHost)) {
            route.virtualHost(this.virtualHost);
        }

        // === HANDLERS WEIGHT IN ORDER ===
        // PLATFORM
        if (apiMeta.timeout() > 0) {
            // PlatformHandler
            route.handler(TimeoutHandler.create(apiMeta.timeout(), apiMeta.statusCodeForTimeout()));
        }
        route.handler(ResponseTimeHandler.create());
        this.platformHandlers.forEach(route::handler);

        //    SECURITY_POLICY,
        // SecurityPolicyHandler
        // CORS: Cross Origin Resource Sharing
        String cors;
        if (this.corsOriginPattern == null) {
            cors = apiMeta.corsOriginPattern();
        } else {
            cors = this.corsOriginPattern;
        }
        if (cors != null && !Objects.equals(cors, "")) {
            if (Objects.equals(cors, "*")) {
                route.handler(CorsHandler.create());
            } else {
                route.handler(CorsHandler.create(cors));
            }
        }
        this.securityPolicyHandlers.forEach(route::handler);

        //    PROTOCOL_UPGRADE,
        protocolUpgradeHandlers.forEach(route::handler);
        //    BODY,
        if (apiMeta.requestBodyNeeded()) {
            route.handler(BodyHandler.create(uploadDirectory));
        }
        //    MULTI_TENANT,
        multiTenantHandlers.forEach(route::handler);
        //    AUTHENTICATION,
        authenticationHandlers.forEach(route::handler);
        //    INPUT_TRUST,
        inputTrustHandlers.forEach(route::handler);
        //    AUTHORIZATION,
        authorizationHandlers.forEach(route::handler);
        //    USER
        userHandlers.forEach(route::handler);

        // finally!
        route.handler(handler);
        return this;
    }

    public Router getRouter() {
        return router;
    }
}
