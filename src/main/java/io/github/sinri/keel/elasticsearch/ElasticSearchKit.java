package io.github.sinri.keel.elasticsearch;

import io.github.sinri.keel.elasticsearch.index.ESIndexMixin;
import io.github.sinri.keel.logger.event.KeelEventLogger;

/**
 * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/8.9/rest-apis.html">ES Restful API 8.9</a>
 * Here only JSON over HTTP(s) supported.
 * @since 3.0.7
 */
public class ElasticSearchKit implements ESApiMixin, ESIndexMixin {
    private final ElasticSearchConfig esConfig;
    private final KeelEventLogger logger;

    public ElasticSearchKit(String esKey, KeelEventLogger logger) {
        this.esConfig = new ElasticSearchConfig(esKey);
        this.logger = logger;
    }

    public ElasticSearchConfig getEsConfig() {
        return esConfig;
    }

    public KeelEventLogger getLogger() {
        return logger;
    }

}
