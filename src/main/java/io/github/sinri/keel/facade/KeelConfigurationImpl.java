package io.github.sinri.keel.facade;

import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Properties;

public class KeelConfigurationImpl implements KeelConfiguration {

    private JsonObject data = new JsonObject();

    public KeelConfigurationImpl() {
        this.data = new JsonObject();
    }

    public KeelConfigurationImpl(@Nonnull JsonObject jsonObject) {
        this.data = jsonObject;
    }

    public KeelConfigurationImpl(@Nonnull Properties properties) {
        this.data = KeelConfiguration.transformPropertiesToJsonObject(properties);
    }

    /**
     * Just a non-deep merge in.
     */
    @Override
    public KeelConfiguration putAll(JsonObject datum) {
        data.mergeIn(datum);
        return this;
    }


    @Override
    public @NotNull JsonObject toJsonObject() {
        return this.data;
    }

    @Override
    public @NotNull KeelConfiguration reloadDataFromJsonObject(JsonObject data) {
        this.data = data;
        return this;
    }

}
