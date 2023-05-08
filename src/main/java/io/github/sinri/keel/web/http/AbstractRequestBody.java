package io.github.sinri.keel.web.http;

import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.vertx.ext.web.RoutingContext;

/**
 * @since 3.0.1
 */
abstract public class AbstractRequestBody extends SimpleJsonifiableEntity {
    public AbstractRequestBody(RoutingContext routingContext) {
        super(routingContext.body().asJsonObject());
    }
}
