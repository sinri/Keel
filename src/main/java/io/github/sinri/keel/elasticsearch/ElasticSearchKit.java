package io.github.sinri.keel.elasticsearch;

import io.github.sinri.keel.elasticsearch.index.ESIndexMixin;
import io.github.sinri.keel.logger.event.KeelEventLogger;

import javax.annotation.Nonnull;

/**
 * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/8.9/rest-apis.html">ES Restful API 8.9</a>
 * Here only JSON over HTTP(s) supported.
 * @since 3.0.7
 */
public class ElasticSearchKit implements ESApiMixin, ESIndexMixin {
    private final ElasticSearchConfig esConfig;
    /**
     * @since 3.2.0
     */
    private final KeelEventLogger eventLogger;

    /**
     * @since 3.2.0 replace KeelEventLogger with KeelRoutineIssueRecorder.
     */
    public ElasticSearchKit(@Nonnull String esKey, @Nonnull KeelEventLogger eventLogger) {
        this.esConfig = new ElasticSearchConfig(esKey);
        this.eventLogger = eventLogger;
    }

    public ElasticSearchConfig getEsConfig() {
        return esConfig;
    }

    /**
     * @since 3.2.0
     */
    @Override
    public KeelEventLogger getLogger() {
        return eventLogger;
    }
}
