package io.github.sinri.keel.facade.cluster.pur.config;

import io.github.sinri.keel.core.TechnicalPreview;
import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.vertx.core.json.JsonArray;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

/**
 * @since 3.1.3
 */
@TechnicalPreview(since = "3.1.3")
public class KeelPurConfig extends SimpleJsonifiableEntity {

    @Nonnull
    public List<Integer> getPorts() {
        var ports = Objects.requireNonNull(this.readIntegerArray("ports"));
        if (ports.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return ports;
    }

    public KeelPurConfig setPorts(@Nonnull List<Integer> ports) {
        if (ports.isEmpty()) throw new IllegalArgumentException();
        var x = new JsonArray(ports);
        this.jsonObject.put("ports", x);
        return this;
    }

    @Nonnull
    public List<String> getAddresses() {
        var addresses = Objects.requireNonNull(this.readStringArray("addresses"));
        if (addresses.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return addresses;
    }

    public KeelPurConfig setAddresses(@Nonnull List<String> addresses) {
        if (addresses.isEmpty()) throw new IllegalArgumentException();
        var x = new JsonArray(addresses);
        this.jsonObject.put("addresses", x);
        return this;
    }

    @Nonnull
    public String getListenHost() {
        return Objects.requireNonNullElse(readString("listen_host"), "0.0.0.0");
    }

    public KeelPurConfig setListenHost(@Nonnull String listenHost) {
        this.jsonObject.put("listen_host", listenHost);
        return this;
    }
}
