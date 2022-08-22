package io.github.sinri.keel.test.servant;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.servant.sisiodosi.KeelSisiodosi;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.Future;

public class KeelSisiodosiTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        KeelLogger logger = Keel.outputLogger("main");

        KeelSisiodosi sisiodosi = new KeelSisiodosi("KeelSisiodosiTestAddress");

        for (int i = 0; i < 100; i++) {
            int finalI = i;
            sisiodosi.drop(v -> {
                logger.info("DRIP " + finalI);
                return Future.succeededFuture();
            });
        }
    }
}
