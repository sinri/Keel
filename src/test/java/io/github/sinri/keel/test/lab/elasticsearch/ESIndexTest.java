package io.github.sinri.keel.test.lab.elasticsearch;

import io.github.sinri.keel.elasticsearch.ESApiMixin;
import io.github.sinri.keel.elasticsearch.ElasticSearchKit;
import io.github.sinri.keel.logger.event.legacy.center.KeelOutputEventLogCenter;
import io.github.sinri.keel.logger.issue.center.KeelIssueRecordCenter;
import io.vertx.core.Future;
import io.vertx.core.VertxOptions;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class ESIndexTest {
    public static void main(String[] args) {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        Keel.initializeVertx(new VertxOptions());

        test1()
                .onFailure(throwable -> {
                    KeelOutputEventLogCenter.instantLogger().exception(throwable);
                })
                .eventually(() -> {
                    return Keel.getVertx().close();
                });
    }

    private static Future<Void> test1() {
        var logger = KeelIssueRecordCenter.outputCenter().generateEventLogger("ESIndexTest");
        ElasticSearchKit es = new ElasticSearchKit("kumori");

        return es.indexGet("kumori-es-test-1", new ESApiMixin.ESApiQueries())
                .compose(resp -> {
                    logger.info(log -> log.message("API RESPONSE")
                            .context(c -> c.put("resp", resp)));
                    return Future.succeededFuture();
                });
    }
}
