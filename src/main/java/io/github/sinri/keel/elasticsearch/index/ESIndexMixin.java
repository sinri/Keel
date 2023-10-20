package io.github.sinri.keel.elasticsearch.index;

import io.github.sinri.keel.elasticsearch.ESApiMixin;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

/**
 * @since 3.0.7
 */
public interface ESIndexMixin extends ESApiMixin {
    /**
     * @param indexName (Required, string) Comma-separated list of data streams, indices, and aliases used to limit the request. Supports wildcards (*). To target all data streams and indices, omit this parameter or use * or _all.
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/8.9/indices-get-index.html">Get index API</a>
     * If the Elasticsearch security features are enabled, you must have the view_index_metadata or manage index privilege for the target data stream, index, or alias.
     */
    default Future<ESIndexGetResponse> indexGet(String indexName, ESApiQueries queries) {
        return call(HttpMethod.GET, "/" + indexName, queries, null)
                .compose(resp -> {
                    return Future.succeededFuture(new ESIndexGetResponse(resp));
                });
    }

    /**
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/8.9/indices-create-index.html">Create index API</a>
     */
    default Future<ESIndexCreateResponse> indexCreate(String indexName, ESApiQueries queries, JsonObject requestBody) {
        return call(HttpMethod.PUT, "/" + indexName, queries, requestBody)
                .compose(resp -> {
                    return Future.succeededFuture(new ESIndexCreateResponse(resp));
                });
    }

    /**
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/8.9/indices-delete-index.html">Delete index API</a>
     */
    default Future<ESIndexDeleteResponse> indexDelete(String indexName, ESApiQueries queries) {
        return call(HttpMethod.DELETE, "/" + indexName, queries, null)
                .compose(resp -> {
                    return Future.succeededFuture(new ESIndexDeleteResponse(resp));
                });
    }
}
