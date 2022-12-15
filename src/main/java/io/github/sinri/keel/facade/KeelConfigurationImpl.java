package io.github.sinri.keel.facade;

import io.vertx.core.json.JsonObject;

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
    public JsonObject toJsonObject() {
        return this.data;
    }

    @Override
    public KeelConfiguration reloadDataFromJsonObject(JsonObject data) {
        this.data = data;
        return this;
    }

}
