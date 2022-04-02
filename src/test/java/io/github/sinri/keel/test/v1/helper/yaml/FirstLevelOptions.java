package io.github.sinri.keel.test.v1.helper.yaml;

import io.github.sinri.keel.core.properties.KeelOptions;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class FirstLevelOptions extends KeelOptions {

    private String host;
    private int port;
    private List<SecondLevelOptions> filters = new ArrayList<>();

    public FirstLevelOptions() {
        super();
        initializeProperties();
    }

    public FirstLevelOptions(JsonObject jsonObject) {
        super();
        initializeProperties();
        overwritePropertiesWithJsonObject(jsonObject);
    }

    public String getHost() {
        return host;
    }

    public FirstLevelOptions setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public FirstLevelOptions setPort(int port) {
        this.port = port;
        return this;
    }

    public List<SecondLevelOptions> getFilters() {
        return filters;
    }

    public FirstLevelOptions setFilters(List<SecondLevelOptions> filters) {
        this.filters = filters;
        return this;
    }

    protected void initializeProperties() {
        host = "127.0.0.1";
        port = 80;
        filters = new ArrayList<>();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("FirstLevelOptions{host=" + host + ";port=" + port + ";filters=[");
        for (var filter : filters) {
            sb.append("<").append(filter).append(">");
        }
        sb.append("]}");
        return sb.toString();
    }
}
