package io.github.sinri.keel.test.lab.elasticsearch;

import io.github.sinri.keel.elasticsearch.ESApiMixin;
import io.github.sinri.keel.elasticsearch.ElasticSearchKit;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import org.jetbrains.annotations.NotNull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class ESCatIndicesTest extends KeelTest {
    @NotNull
    @Override
    protected Future<Void> starting() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        return super.starting();
    }

    @TestUnit
    public Future<Void> test() {
        // {"client_code":"ai-test","timestamp":1712023360984,"checksum":"d6abf7d98af34907d97f6a6578a429b5","http_method":"GET","endpoint":"/_cat/indices"}
        ESApiMixin.ESApiQueries esApiQueries = new ESApiMixin.ESApiQueries();
        //esApiQueries.put("format", "application/json");
        return new ElasticSearchKit("kumori")
                .call(
                        HttpMethod.GET,
                        //"/_cat/indices",
                        "/*",
                        esApiQueries,
                        null
                )
                .compose(resp -> {
                    getLogger().info("resp", resp);
                    return Future.succeededFuture();
                });
    }
}
