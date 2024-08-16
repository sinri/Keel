package io.github.sinri.keel.elasticsearch;

import io.github.sinri.keel.facade.configuration.KeelConfigElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

import static io.github.sinri.keel.facade.KeelInstance.Keel;

/**
 * @since 3.0.7
 */
public class ElasticSearchConfig extends KeelConfigElement {

    public ElasticSearchConfig(KeelConfigElement configuration) {
        super(configuration);
    }

    public ElasticSearchConfig(String esKey) {
        this(Keel.getConfiguration().extract("es", esKey));
    }

    /*
    String esUsername = Keel.config("es.kumori.username");
        String esPassword = Keel.config("es.kumori.password");
        String esClusterHost = Keel.config("es.kumori.cluster.host");
        int esClusterPort = Objects.requireNonNullElse(Integer.valueOf(Keel.config("es.kumori.cluster.port")), 9200);
        String esClusterScheme = Objects.requireNonNullElse(Keel.config("es.kumori.cluster.scheme"), "http");
        int esPoolSize = Objects.requireNonNullElse(Integer.valueOf(Keel.config("es.kumori.pool.size")), 16);
        int esRestMaxConnection = Objects.requireNonNullElse(Integer.valueOf(Keel.config("es.kumori.rest.maxConnection")), 320);
        int esRestMaxConnectionPerRoute = Objects.requireNonNullElse(Integer.valueOf(Keel.config("es.kumori.rest.maxConnectionPerRoute")), 160);

     */

    public String username() {
        return readString("username", null);
    }

    public String password() {
        return readString("password", null);
    }

    public @NotNull String clusterHost() {
        return Objects.requireNonNull(readString(List.of("cluster", "host"), null));
    }

    public int clusterPort() {
        return readInteger(List.of("cluster", "port"), 9200);
    }

    public @NotNull String clusterScheme() {
        return Objects.requireNonNull(readString(List.of("cluster", "scheme"), "http"));
    }

    public @NotNull String clusterApiUrl(@NotNull String endpoint) {
        return this.clusterScheme() + "://" + this.clusterHost() + ":" + this.clusterPort() + endpoint;
    }

    public @Nullable String opaqueId() {
        return readString("opaqueId", null);
    }
}
