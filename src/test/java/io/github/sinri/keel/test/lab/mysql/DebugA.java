package io.github.sinri.keel.test.lab.mysql;

import io.github.sinri.keel.mysql.KeelMySQLConfiguration;
import io.github.sinri.keel.tesuto.KeelTest;
import io.github.sinri.keel.tesuto.TestUnit;
import io.vertx.core.Future;
import org.jetbrains.annotations.NotNull;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

public class DebugA extends KeelTest {

    @NotNull
    @Override
    protected Future<Void> starting() {
        Keel.getConfiguration().loadPropertiesFile("config.properties");
        return Future.succeededFuture();
    }

    @TestUnit
    public Future<Void> test1() {
        KeelMySQLConfiguration pioneerConfig = KeelMySQLConfiguration.loadConfigurationForDataSource(Keel.getConfiguration(), "pioneer");
        return pioneerConfig.instantQuery(
                        "select 1"
                )
                .compose(resultMatrix -> {
                    getLogger().info("lalala: ", j -> j.put("result_matrix", resultMatrix.toJsonArray()));
                    return Future.succeededFuture();
                });
    }
}
