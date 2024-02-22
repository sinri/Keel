package io.github.sinri.keel.test.dysonsphere.receptionist;

import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;
import io.github.sinri.keel.web.http.ApiMeta;
import io.github.sinri.keel.web.http.receptionist.KeelWebFutureReceptionist;
import io.github.sinri.keel.web.http.receptionist.ReceptionistIssueRecord;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

@ApiMeta(routePath = "/receptionist/test", allowMethods = {"GET", "POST"})
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

    @Override
    protected KeelIssueRecorder<ReceptionistIssueRecord> createReceptionistIssueRecorder() {
        return KeelIssueRecordCenter.outputCenter().generateRecorder(ReceptionistIssueRecord.TopicReceptionist, ReceptionistIssueRecord::new);
    }
}
