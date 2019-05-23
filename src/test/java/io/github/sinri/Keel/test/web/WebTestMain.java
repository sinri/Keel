package io.github.sinri.Keel.test.web;

import io.github.sinri.Keel.Keel;

public class WebTestMain {
    public static void main(String[] args) {
        TestHttpServerVerticle testHttpServerVerticle = new TestHttpServerVerticle();
        Keel.getVertX().deployVerticle(testHttpServerVerticle, res -> {
            Keel.getLogger(WebTestMain.class).info("deployVerticle for testHttpServerVerticle: " + res.result());
        });
    }
}
