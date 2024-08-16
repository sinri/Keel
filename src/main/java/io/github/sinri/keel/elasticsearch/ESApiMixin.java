package io.github.sinri.keel.elasticsearch;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Objects;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * Developed with ES version 8.9.
 *
 * @since 3.0.7
 */
public interface ESApiMixin {
    ElasticSearchConfig getEsConfig();

    /**
     * @since 3.1.10
     * For Bulk API, of which the body is not a json object.
     */
    default Future<JsonObject> call(@NotNull HttpMethod httpMethod, @NotNull String endpoint, @Nullable ESApiQueries queries, @Nullable String requestBody) {
        WebClient webClient = WebClient.create(Keel.getVertx());
        String url = this.getEsConfig().clusterApiUrl(endpoint);
        HttpRequest<Buffer> bufferHttpRequest = webClient.requestAbs(httpMethod, url);

        bufferHttpRequest.basicAuthentication(getEsConfig().username(), getEsConfig().password());
        bufferHttpRequest.putHeader("Accept", "application/vnd.elasticsearch+json");
        bufferHttpRequest.putHeader("Content-Type", "application/vnd.elasticsearch+json");

        String opaqueId = this.getEsConfig().opaqueId();
        if (opaqueId != null) {
            bufferHttpRequest.putHeader("X-Opaque-Id", opaqueId);
        }

        if (queries != null) {
            queries.forEach(bufferHttpRequest::addQueryParam);
        }

//        Handler<KeelEventLog> logRequestEnricher = log -> log
//                .context(c -> c
//                        .put("request", new JsonObject()
//                                .put("method", httpMethod.name())
//                                .put("endpoint", endpoint)
//                                .put("queries", queriesForLog)
//                                .put("body", requestBody)
//                        )
//                );

        return Future.succeededFuture()
                .compose(v -> {
                    if (httpMethod == HttpMethod.GET || httpMethod == HttpMethod.DELETE) {
                        return bufferHttpRequest.send();
                    } else {
                        return bufferHttpRequest.sendBuffer(Buffer.buffer(Objects.requireNonNullElse(requestBody, "")));
                    }
                })
                .compose(bufferHttpResponse -> {
                    int statusCode = bufferHttpResponse.statusCode();
                    if ((statusCode >= 300 || statusCode < 200)) {
//                        this.getLogger().error(log -> {
//                            logRequestEnricher.handle(log);
//                            log.message("ES API Response Error")
//                                    .context(c -> c
//                                            .put("response", new JsonObject()
//                                                    .put("status_code", statusCode)
//                                                    .put("raw", bufferHttpResponse.bodyAsString()))
//                                    );
//                        });
//                        return Future.failedFuture("ES API: STATUS CODE IS " + statusCode + " | " + bufferHttpResponse.bodyAsString());

                        return Future.failedFuture(new ESApiException(
                                statusCode, bufferHttpResponse.bodyAsString(),
                                httpMethod,
                                endpoint,
                                queries,
                                requestBody
                        ));
                    }
                    JsonObject resp;
                    try {
                        resp = bufferHttpResponse.bodyAsJsonObject();
                    } catch (DecodeException decodeException) {
                        // There are situations that use Json Array as the response body!
                        resp = new JsonObject()
                                .put("array", new JsonArray(bufferHttpResponse.bodyAsString()));
                    }
//                    this.getLogger().info(log -> {
//                        logRequestEnricher.handle(log);
//                        log.message("ES API Response Error")
//                                .context(c -> c
//                                        .put("response", new JsonObject()
//                                                .put("status_code", statusCode)
//                                                .put("body", resp))
//                                );
//                    });
                    return Future.succeededFuture(resp);
                });
    }

    /**
     * @since 3.1.10 based on `io.github.sinri.keel.elasticsearch.ESApiMixin#call(io.vertx.core.http.HttpMethod, java.lang.String, io.github.sinri.keel.elasticsearch.ESApiMixin.ESApiQueries, java.lang.String)`
     */
    default Future<JsonObject> callPost(@NotNull String endpoint, @Nullable ESApiQueries queries, @NotNull JsonObject requestBody) {
        return call(HttpMethod.POST, endpoint, queries, requestBody.toString());
    }

//    default Future<JsonObject> call(HttpMethod httpMethod, String endpoint, ESApiQueries queries, @Nullable JsonObject requestBody) {
//        WebClient webClient = WebClient.create(Keel.getVertx());
//        String url = this.getEsConfig().clusterApiUrl(endpoint);
//        HttpRequest<Buffer> bufferHttpRequest = webClient.requestAbs(httpMethod, url);
//
//        bufferHttpRequest.basicAuthentication(getEsConfig().username(), getEsConfig().password());
//        bufferHttpRequest.putHeader("Accept", "application/vnd.elasticsearch+json");
//        bufferHttpRequest.putHeader("Content-Type", "application/vnd.elasticsearch+json");
//
//        String opaqueId = this.getEsConfig().opaqueId();
//        if (opaqueId != null) {
//            bufferHttpRequest.putHeader("X-Opaque-Id", opaqueId);
//        }
//
//        JsonObject queriesForLog = new JsonObject();
//        if (queries != null) {
//            queries.forEach((k, v) -> {
//                bufferHttpRequest.addQueryParam(k, v);
//                queriesForLog.put(k, v);
//            });
//        }
//
//        Handler<KeelEventLog> logRequestEnricher = log -> log
//                .context(c -> c
//                        .put("request", new JsonObject()
//                                .put("method", httpMethod.name())
//                                .put("endpoint", endpoint)
//                                .put("queries", queriesForLog)
//                                .put("body", requestBody)
//                        )
//                );
//
//        return Future.succeededFuture()
//                .compose(v -> {
//                    if (httpMethod == HttpMethod.GET) {
//                        return bufferHttpRequest.send();
//                    } else {
//                        return bufferHttpRequest.sendJsonObject(requestBody);
//                    }
//                })
//                .compose(bufferHttpResponse -> {
//                    int statusCode = bufferHttpResponse.statusCode();
//                    JsonObject resp = bufferHttpResponse.bodyAsJsonObject();
//
//                    if ((statusCode >= 300 || statusCode < 200) || resp == null) {
//                        this.getLogger().error(log -> {
//                            logRequestEnricher.handle(log);
//                            log.message("ES API Response Error")
//                                    .context(c -> c
//                                            .put("response", new JsonObject()
//                                                    .put("status_code", statusCode)
//                                                    .put("raw", bufferHttpResponse.bodyAsString()))
//                                    );
//                        });
//                        return Future.failedFuture("ES API: STATUS CODE IS " + statusCode + " | " + bufferHttpResponse.bodyAsString());
//                    }
//                    this.getLogger().info(log -> {
//                        logRequestEnricher.handle(log);
//                        log.message("ES API Response Error")
//                                .context(c -> c
//                                        .put("response", new JsonObject()
//                                                .put("status_code", statusCode)
//                                                .put("body", resp))
//                                );
//                    });
//                    return Future.succeededFuture(resp);
//                });
//    }

    class ESApiQueries extends HashMap<String, String> {
        public JsonObject toJsonObject() {
            JsonObject jsonObject = new JsonObject();
            this.forEach(jsonObject::put);
            return jsonObject;
        }
    }

    class ESApiException extends Exception {
        private final int statusCode;
        private final @Nullable String response;

        private final @NotNull HttpMethod httpMethod;
        private final @NotNull String endpoint;
        private final @Nullable ESApiQueries queries;
        private final @Nullable String requestBody;

        public ESApiException(
                int statusCode, @Nullable String response,
                @NotNull HttpMethod httpMethod,
                @NotNull String endpoint,
                @Nullable ESApiQueries queries,
                @Nullable String requestBody
        ) {
            this.statusCode = statusCode;
            this.response = response;

            this.httpMethod = httpMethod;
            this.endpoint = endpoint;
            this.queries = queries;
            this.requestBody = requestBody;
        }

        @Override
        public String toString() {
            return getClass().getName()
                    + "{status_code: " + statusCode
                    + ", response: " + response
                    + ", http_method: " + httpMethod.name()
                    + ", endpoint: " + endpoint
                    + ", queries: " + queries
                    + ", request_body: " + requestBody
                    + "}";
        }

        public int getStatusCode() {
            return statusCode;
        }

        @Nullable
        public String getResponse() {
            return response;
        }

        @NotNull
        public String getEndpoint() {
            return endpoint;
        }

        @Nullable
        public ESApiQueries getQueries() {
            return queries;
        }

        @NotNull
        public HttpMethod getHttpMethod() {
            return httpMethod;
        }

        @Nullable
        public String getRequestBody() {
            return requestBody;
        }
    }
}
