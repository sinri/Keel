package io.github.sinri.keel.test.web.receptionist;

import io.github.sinri.keel.web.KeelWebRequestFilter;
import io.github.sinri.keel.web.KeelWebRequestReceptionist;
import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;

public class RootPathReceptionist extends KeelWebRequestReceptionist<String> {

    public RootPathReceptionist(RoutingContext routingContext) {
        super(routingContext);
    }

    @Override
    protected List<Class<? extends KeelWebRequestFilter>> getFilterClassList() {
        return new ArrayList<>();
    }

    @Override
    protected Future<String> dealWithRequest() {
        return Future.succeededFuture("done");
    }

//    @Override
//    public Route registerRoute(Router router) {
//        // set path and virtual host
//        // and body handler (except self)
//        return router.route("/");
//    }
}
