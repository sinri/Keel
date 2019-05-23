package io.github.sinri.Keel.web;

import io.github.sinri.Keel.Keel;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class KeelHttpRequestHandler {
    protected HttpServerRequest rawRequest;
    protected String uriHost;
    protected Integer uriPort;

    protected Map<String, String> queries;
//    protected Map<String,String> headers;

    public KeelHttpRequestHandler(HttpServerRequest request) {
        setRawRequest(request);
    }

    public HttpServerRequest getRawRequest() {
        return rawRequest;
    }

    public void setRawRequest(HttpServerRequest rawRequest) {
        this.rawRequest = rawRequest;
        if (rawRequest.host() != null) {
            String[] parts = rawRequest.host().split(":");
            uriHost = parts[0];
            if (parts.length > 1) {
                uriPort = Integer.parseInt(parts[1]);
            }
        }
        queries = null;
//        headers=null;
    }

    public HttpMethod getMethodEnum() {
        return rawRequest.method();
    }

    public String getMethod() {
        return rawRequest.rawMethod();
    }

    public String getUriHost() {
        return uriHost;
    }

    public Integer getUriPort() {
        return uriPort;
    }

    public String getScheme() {
        return rawRequest.scheme();
    }

    public String getPath() {
        return rawRequest.path();
    }

    public Map<String, String> getQueries() {
        if (queries == null) {
            String query = rawRequest.query();
            Keel.getLogger(this.getClass()).fine("raw query: " + query);
            String[] pairs = query.split("&");

            queries = new HashMap<>();
            if (pairs.length > 0)
                for (String pair : pairs) {
                    if (pair == null || "".equalsIgnoreCase(pair)) continue;
                    Keel.getLogger(this.getClass()).fine("raw query pair: " + pair);
                    String[] kv = pair.split("=");
                    if (kv.length > 0) {
                        String value = kv.length > 1 ? kv[1] : "";
                        try {
                            value = URLDecoder.decode(value, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            //e.printStackTrace();
                            Keel.getLogger(this.getClass()).warning(e.getMessage());
                        }
                        queries.put(kv[0], value);
                    }
                }
        }
        return queries;
    }

    public String getQuery(String key) {
        return rawRequest.getParam(key);
    }

    public String getHeader(String key) {
        return rawRequest.getHeader(key);
    }

    public void doAfterReadingWholeBodyAsJson(Handler<JsonObject> bodyHandler) {
        rawRequest.bodyHandler(totalBuffer -> {
            JsonObject jsonObject = totalBuffer.toJsonObject();
            bodyHandler.handle(jsonObject);
        });
    }

    public void respondAsJson(JsonObject jsonObject, int code) {
        rawRequest.response()
                .putHeader("Content-Type", "application/json;charset: UTF-8")
                .setStatusCode(code)
                .end(jsonObject.toString());
    }

    public void repondOkAsJson(JsonObject data) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("code", "OK").put("data", data);
        respondAsJson(jsonObject, 200);
    }

    public void repondOkAsJson(String data) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("code", "OK").put("data", data);
        respondAsJson(jsonObject, 200);
    }

    public void repondFailAsJson(JsonObject data) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("code", "FAIL").put("data", data);
        respondAsJson(jsonObject, 200);
    }

    public void repondFailAsJson(String data) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("code", "FAIL").put("data", data);
        respondAsJson(jsonObject, 200);
    }


}
