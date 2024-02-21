package io.github.sinri.keel.elasticsearch.document;

import io.github.sinri.keel.elasticsearch.ESApiMixin;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public interface ESDocumentMixin extends ESApiMixin {
    default Future<ESDocumentCreateResponse> documentCreate(String indexName, @Nullable String documentId, @Nullable ESApiQueries queries, JsonObject documentBody) {
        return Future.succeededFuture()
                .compose(v -> {
                    if (documentId == null) {
                        return callPost("/" + indexName + "/_doc/", queries, documentBody);
                    } else {
                        return callPost("/" + indexName + "/_create/" + documentId, queries, documentBody);
                    }
                })
                .compose(resp -> {
                    return Future.succeededFuture(new ESDocumentCreateResponse(resp));
                });
    }

    /**
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-get.html">Get API</a>
     */
    default Future<ESDocumentGetResponse> documentGet(String indexName, String documentId, @Nullable ESApiQueries queries) {
        return call(HttpMethod.GET, "/" + indexName + "/_doc/" + documentId, queries, null)
                .compose(resp -> {
                    return Future.succeededFuture(new ESDocumentGetResponse(resp));
                });
    }

    /**
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-delete.html">Delete API</a>
     */
    default Future<ESDocumentDeleteResponse> documentDelete(String indexName, String documentId, @Nullable ESApiQueries queries) {
        return call(HttpMethod.DELETE, "/" + indexName + "/_doc/" + documentId, queries, null)
                .compose(resp -> {
                    return Future.succeededFuture(new ESDocumentDeleteResponse(resp));
                });
    }

    /**
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-update.html">Update API</a>
     */
    default Future<ESDocumentUpdateResponse> documentUpdate(String indexName, String documentId, @Nullable ESApiQueries queries, JsonObject requestBody) {
        return callPost("/" + indexName + "/_update/" + documentId, queries, requestBody)
                .compose(resp -> {
                    return Future.succeededFuture(new ESDocumentUpdateResponse(resp));
                });
    }

    /**
     * Performs multiple indexing or delete operations in a single API call.
     * This reduces overhead and can greatly increase indexing speed.
     *
     * @param target (Optional, string) Name of the data stream, index, or index alias to perform bulk actions on.
     * @see <a href="https://www.elastic.co/guide/en/elasticsearch/reference/current/docs-bulk.html">Bulk API</a>
     * @since 3.1.10
     */
    default Future<ESDocumentBulkResponse> documentBulk(@Nullable String target, @Nullable ESApiQueries queries, @Nonnull List<JsonObject> requestBody) {
        // POST /_bulk
        // POST /<target>/_bulk
        String endpoint = "/_bulk";
        if (target != null) {
            endpoint = "/" + target + endpoint;
        }
        StringBuilder body = new StringBuilder();
        requestBody.forEach(x -> {
            body.append(x.toString()).append("\n");
        });
        return call(HttpMethod.POST, endpoint, queries, body.toString())
                .compose(resp -> {
                    return Future.succeededFuture(new ESDocumentBulkResponse(resp));
                });
    }
}
