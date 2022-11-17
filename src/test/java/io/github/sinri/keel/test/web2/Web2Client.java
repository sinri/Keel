package io.github.sinri.keel.test.web2;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.ext.web.client.WebClient;

import java.util.ArrayList;
import java.util.List;

public class Web2Client {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        List<Future> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Future<Void> future = call(i);
            list.add(future);
        }

        CompositeFuture.join(list)
                .eventually(v -> {
                    return Keel.getVertx().close();
                })
        ;


    }

    private static Future<Void> call(int i) {
        return WebClient.create(Keel.getVertx())
                .postAbs("http://localhost:8099/a")
                .send()
                .compose(bufferHttpResponse -> {
                    Keel.outputLogger("No." + i).info(bufferHttpResponse.statusCode() + " " + bufferHttpResponse.statusMessage());
                    Keel.outputLogger("No." + i).info(bufferHttpResponse.bodyAsString());
                    return Future.succeededFuture();
                });
    }
}
