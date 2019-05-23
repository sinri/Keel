package io.github.sinri.Keel.test.web;

import io.github.sinri.Keel.Keel;
import io.github.sinri.Keel.web.HttpServerVerticle;
import io.github.sinri.Keel.web.KeelHttpRequestHandler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;

public class TestHttpServerVerticle extends HttpServerVerticle {
    @Override
    protected void initializeRequestHandler(HttpServer server) {
        server.requestHandler(request -> {
            KeelHttpRequestHandler handler = new KeelHttpRequestHandler(request);

//            Keel.getLogger(this.getClass()).info("Method: "+request.method()+" raw: "+request.rawMethod());
//            Keel.getLogger(this.getClass()).info("Uri: "+request.uri()+" absolute: "+request.absoluteURI());
//            Keel.getLogger(this.getClass()).info("Host: "+request.host());
//            Keel.getLogger(this.getClass()).info("Path: "+request.path());
//            Keel.getLogger(this.getClass()).info("Query: "+request.query());
//            Keel.getLogger(this.getClass()).info("Use SSL: "+request.isSSL());
//            Keel.getLogger(this.getClass()).info("Local: "+request.localAddress());
//            Keel.getLogger(this.getClass()).info("Remote: "+request.remoteAddress());

            Keel.getLogger(this.getClass()).info(handler.getMethod() + " " + handler.getScheme() + "://" + handler.getUriHost() + ":" + handler.getUriPort() + " " + handler.getPath());

            Keel.getLogger(this.getClass()).info("Headers: " + handler.getRawRequest().headers());
            Keel.getLogger(this.getClass()).info("Header[X-S]: " + handler.getHeader("X-S"));
            Keel.getLogger(this.getClass()).info("Queries: " + handler.getQueries());
            Keel.getLogger(this.getClass()).info("Query[lalala]: " + handler.getQuery("lalala"));

//            handler.getRawRequest().bodyHandler(buffer->{
//                Keel.getLogger(this.getClass()).info("read body buffer: "+buffer);
//                // The body has now been fully read, so retrieve the form attributes
//                    MultiMap formAttributes = handler.getRawRequest().formAttributes();
//                    Keel.getLogger(this.getClass()).info("Form: "+formAttributes.size());
//            });

            handler.doAfterReadingWholeBodyAsJson(json -> {
                Keel.getLogger(this.getClass()).info("BODY AS JSON: " + json);

                JsonObject json1 = new JsonObject();
                json1.put("code", "OK");
                json1.put("data", json);
                handler.respondAsJson(json1, 200);
            });
        });
    }

    @Override
    protected int getListenPort() {
        return 8000;
    }

    @Override
    protected String getListenAddress() {
        return null;
    }
}
