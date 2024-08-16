package io.github.sinri.keel.test.dysonsphere.receptionist;

import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.web.http.ApiMeta;
import io.github.sinri.keel.web.http.receptionist.KeelWebFutureReceptionist;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.jetbrains.annotations.NotNull;


@ApiMeta(routePath = "/receptionist/test-for-get", allowMethods = {"GET"})
@ApiMeta(routePath = "/receptionist/test-for-post", allowMethods = {"POST"})
public class TestReceptionist extends KeelWebFutureReceptionist {
    public TestReceptionist(RoutingContext routingContext) {
        super(routingContext);
    }

    @Override
    protected Future<Object> handleForFuture() {
        getIssueRecorder().info(r -> r.message("handleForFuture start"));
        JsonObject jsonObject = new JsonObject().put("path", getRoutingContext().request().path());
        getIssueRecorder().info(r -> r.message("handleForFuture ready"));
        return Future.succeededFuture(jsonObject);
    }

    @NotNull
    @Override
    protected KeelIssueRecordCenter issueRecordCenter() {
        return KeelIssueRecordCenter.outputCenter();
    }

}
