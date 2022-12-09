package io.github.sinri.keel.test.web2;

import io.github.sinri.keel.test.SharedTestBootstrap;
import io.github.sinri.keel.web.KeelHttpServer;
import io.github.sinri.keel.web.handler.KeelPlatformHandler;
import io.github.sinri.keel.web.service.KeelWebRequestRouteKit;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.auth.authorization.PermissionBasedAuthorization;
import io.vertx.ext.web.handler.AuthorizationHandler;

public class Web2Server {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v0 -> {
            new KeelHttpServer(new HttpServerOptions().setPort(8099), true)
                    .configureRoutes(router -> {
                        new KeelWebRequestRouteKit<>(Service.class, router)
                                .addPlatformHandler(new KeelPlatformHandler(null))
                                .addAuthorizationHandler(AuthorizationHandler.create(
                                        PermissionBasedAuthorization.create("Permission")
                                ))
                                .loadPackage("io.github.sinri.keel.test.web2.impl");
                    })
                    .listen();
        });


    }
}
