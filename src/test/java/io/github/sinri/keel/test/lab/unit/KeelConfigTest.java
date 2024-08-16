package io.github.sinri.keel.test.lab.unit;

import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class KeelConfigTest extends KeelTest {

    @NotNull
    @Override
    protected Future<Void> starting() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        return Future.succeededFuture();
    }

    @TestUnit
    public Future<Void> readTest() {
        getLogger().info("all", Keel.getConfiguration().toJsonObject());
        getLogger().info("email.smtp.default_smtp_name: " + Keel.config("email.smtp.default_smtp_name"));
        return Future.succeededFuture();
    }
}
