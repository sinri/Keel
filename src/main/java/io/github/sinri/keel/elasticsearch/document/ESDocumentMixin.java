package io.github.sinri.keel.elasticsearch.document;

import io.github.sinri.keel.elasticsearch.ESApiMixin;
import io.vertx.core.Future;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nullable;

public interface ESDocumentMixin extends ESApiMixin {
    default Future<ESDocumentCreateResponse> documentCreate(String indexName, @Nullable String documentId, @Nullable ESApiQueries queries, JsonObject documentBody) {
        return Future.succeededFuture()
                .compose(v -> {
                    if (documentId == null) {
                        return call(HttpMethod.POST, "/" + indexName + "/_doc/", queries, documentBody);
                    } else {
                        return call(HttpMethod.POST, "/" + indexName + "/_create/" + documentId, queries, documentBody);
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
        return call(HttpMethod.POST, "/" + indexName + "/_update/" + documentId, queries, requestBody)
                .compose(resp -> {
                    return Future.succeededFuture(new ESDocumentUpdateResponse(resp));
                });
    }
}
