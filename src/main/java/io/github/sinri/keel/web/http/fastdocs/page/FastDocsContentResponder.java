package io.github.sinri.keel.web.http.fastdocs.page;

import io.vertx.core.Future;
import io.vertx.ext.web.RoutingContext;


public interface FastDocsContentResponder {
    void setRoutingContext(RoutingContext ctx);

    Future<Void> respond();
}
