package io.github.sinri.keel.web.http.handler;

import io.github.sinri.keel.facade.Keel;
import io.github.sinri.keel.web.http.ApiMeta;
import io.github.sinri.keel.web.http.prehandler.KeelPlatformHandler;
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
 * @since 3.0.0 TEST PASSED
 */
public class KeelWebRequestRouteKit<S extends KeelWebRequestHandler> {
    private final Keel keel;
    private final Router router;
    private final Class<S> classOfService;
    private final List<PlatformHandler> platformHandlers = new ArrayList<>();
    private final List<SecurityPolicyHandler> securityPolicyHandlers = new ArrayList<>();
    private final List<ProtocolUpgradeHandler> protocolUpgradeHandlers = new ArrayList<>();
    private final List<MultiTenantHandler> multiTenantHandlers = new ArrayList<>();
    /**
     * Tells who the user is
     */
    private final List<AuthenticationHandler> authenticationHandlers = new ArrayList<>();
    private final List<InputTrustHandler> inputTrustHandlers = new ArrayList<>();
    /**
     * Tells what the user is allowed to do
     */
    private final List<AuthorizationHandler> authorizationHandlers = new ArrayList<>();
    private final List<Handler<RoutingContext>> userHandlers = new ArrayList<>();
    private String uploadDirectory = BodyHandler.DEFAULT_UPLOADS_DIRECTORY;
    private String virtualHost = null;
    /**
     * @since 2.9.2
     */
    private Handler<RoutingContext> failureHandler = null;

    public KeelWebRequestRouteKit(Keel keel, Class<S> classOfService) {
        this.keel = keel;
        this.classOfService = classOfService;
        router = Router.router(keel.getVertx());
    }

    /**
     * @since 2.9.2
     */
    public KeelWebRequestRouteKit<S> setFailureHandler(Handler<RoutingContext> failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }

    public KeelWebRequestRouteKit(Keel keel, Class<S> classOfService, Router router) {
        this.keel = keel;
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
    public KeelWebRequestRouteKit<S> addAuthorizationHandler(AuthorizationHandler handler) {
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

    /**
     * Load all classes inside the given package, and filter out those with ApiMeta, to build routes for them.
     *
     * @param packageName such as "com.leqee.spore.service"
     * @since 2.9.2 return void
     */
    public void loadPackage(String packageName) {
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends S>> allClasses = reflections.getSubTypesOf(classOfService);

        try {
            allClasses.forEach(this::loadClass);
        } catch (Exception e) {
            keel.getInstantEventLogger().exception(e, "KeelWebRequestRouteKit::loadPackage THROWS");
        }
    }

    /**
     * @since 2.9
     * @since 2.9.2 return void
     */
    public void loadClass(Class<? extends S> c) {
        ApiMeta apiMeta = keel.reflectionHelper().getAnnotationOfClass(c, ApiMeta.class);
        if (apiMeta == null) return;

        keel.getInstantEventLogger().debug("KeelWebRequestRouteKit Loading " + c.getName());

        S handler;
        try {
            handler = c.getConstructor(Keel.class).newInstance(keel);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            keel.getInstantEventLogger().exception(e, "HANDLER REFLECTION EXCEPTION");
            return;
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
        route.handler(new KeelPlatformHandler(keel));
        if (apiMeta.timeout() > 0) {
            // PlatformHandler
            route.handler(TimeoutHandler.create(apiMeta.timeout(), apiMeta.statusCodeForTimeout()));
        }
        route.handler(ResponseTimeHandler.create());
        this.platformHandlers.forEach(route::handler);

        //    SECURITY_POLICY,
        // SecurityPolicyHandler
        // CorsHandler: Cross Origin Resource Sharing
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

        // failure handler since 2.9.2
        if (failureHandler != null) {
            route.failureHandler(failureHandler);
        }
    }

    public Router getRouter() {
        return router;
    }
}
