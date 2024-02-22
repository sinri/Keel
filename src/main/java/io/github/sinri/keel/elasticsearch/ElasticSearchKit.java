package io.github.sinri.keel.elasticsearch;

import io.github.sinri.keel.elasticsearch.index.ESIndexMixin;
import io.github.sinri.keel.logger.issue.record.event.RoutineIssueRecord;
import io.github.sinri.keel.logger.issue.recorder.KeelIssueRecorder;

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
    private final KeelIssueRecorder<RoutineIssueRecord> routineIssueRecorder;

    /**
     * @since 3.2.0 replace KeelEventLogger with KeelRoutineIssueRecorder.
     */
    public ElasticSearchKit(@Nonnull String esKey, @Nonnull KeelIssueRecorder<RoutineIssueRecord> routineIssueRecorder) {
        this.esConfig = new ElasticSearchConfig(esKey);
        this.routineIssueRecorder = routineIssueRecorder;
    }

    public ElasticSearchConfig getEsConfig() {
        return esConfig;
    }

    /**
     * @since 3.2.0
     */
    @Override
    public KeelIssueRecorder<RoutineIssueRecord> getRoutineIssueRecorder() {
        return routineIssueRecorder;
    }
}
