package io.github.sinri.keel.facade;

import io.github.sinri.keel.core.json.JsonifiableEntity;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.github.sinri.keel.facade.KeelInstance.Keel;
import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

/**
 * @since 3.0.0
 */
public class KeelConfiguration implements JsonifiableEntity<KeelConfiguration> {

    private @Nonnull JsonObject data = new JsonObject();

    public KeelConfiguration() {
    }

    /**
     * @param another
     * @since 3.0.7
     */
    public KeelConfiguration(@Nonnull KeelConfiguration another) {
        this(another.toJsonObject());
    }

    public KeelConfiguration(@Nonnull JsonObject jsonObject) {
        this.data = jsonObject;
    }

    public KeelConfiguration(@Nonnull Properties properties) {
        this.data = KeelConfiguration.transformPropertiesToJsonObject(properties);
    }

    static @Nonnull JsonObject transformPropertiesToJsonObject(@Nonnull Properties properties) {
        JsonObject jsonObject = new JsonObject();

        Set<String> plainKeySet = new HashSet<>();
        properties.forEach((key, value) -> plainKeySet.add(key.toString()));

        for (var plainKey : plainKeySet) {
            String[] components = plainKey.split("\\.");
            List<Object> keychain = Arrays.asList(components);
            try {
                KeelHelpers.jsonHelper().writeIntoJsonObject(jsonObject, keychain, properties.getProperty(plainKey));
            } catch (Throwable throwable) {
                Keel.getLogger().exception(throwable, "io.github.sinri.keel.facade.KeelConfiguration.transformPropertiesToJsonObject Format Failed" + properties);
            }
        }
        return jsonObject;
    }

    static @Nonnull KeelConfiguration createFromPropertiesFile(@Nonnull String propertiesFileName) {
        KeelConfiguration p = new KeelConfiguration();
        p.loadPropertiesFile(propertiesFileName);
        return p;
    }

    static @Nonnull KeelConfiguration createFromProperties(@Nonnull Properties properties) {
        KeelConfiguration p = new KeelConfiguration();
        p.putAll(properties);
        return p;
    }

    static @Nonnull KeelConfiguration createFromJsonObject(@Nonnull JsonObject jsonObject) {
        KeelConfiguration p = new KeelConfiguration();
        p.putAll(jsonObject);
        return p;
    }

    public @Nonnull KeelConfiguration putAll(@Nonnull KeelConfiguration keelConfiguration) {
        return putAll(keelConfiguration.toJsonObject());
    }

    public @Nonnull KeelConfiguration putAll(@Nonnull Properties properties) {
        return putAll(KeelConfiguration.transformPropertiesToJsonObject(properties));
    }

    public @Nonnull KeelConfiguration putAll(@Nonnull JsonObject jsonObject) {
        data.mergeIn(jsonObject);
        return this;
    }

    /**
     * @since 3.0.10
     */
    public @Nonnull KeelConfiguration loadJsonObjectFile(@Nonnull String jsonObjectFileName) {
        try {
            byte[] bytes = KeelHelpers.fileHelper().readFileAsByteArray(jsonObjectFileName, true);
            this.data = new JsonObject(Buffer.buffer(bytes));
            return this;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @since 3.0.1
     */
    public @Nonnull KeelConfiguration loadPropertiesFile(@Nonnull String propertiesFileName) {
        return loadPropertiesFile(propertiesFileName, StandardCharsets.UTF_8);
    }

    public @Nonnull KeelConfiguration loadPropertiesFile(@Nonnull String propertiesFileName, @Nonnull Charset charset) {
        Properties properties = new Properties();
        try {
            // here, the file named as `propertiesFileName` should be put along with JAR
            properties.load(new FileReader(propertiesFileName, charset));
        } catch (IOException e) {
            System.err.println("Cannot find the file config.properties. Use the embedded one.");
            try {
                properties.load(KeelConfiguration.class.getClassLoader().getResourceAsStream(propertiesFileName));
            } catch (IOException ex) {
                throw new RuntimeException("Cannot find the embedded file config.properties.", ex);
            }
        }

        return putAll(properties);
    }

    /**
     * @since 3.0.6
     */
    public @Nonnull KeelConfiguration loadPropertiesFileContent(@Nonnull String content) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(content));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load given properties content.", e);
        }
        return putAll(properties);
    }

    public @Nonnull KeelConfiguration extract(String... keychain) {
        JsonObject jsonObject = Objects.requireNonNullElse(readJsonObject(keychain), new JsonObject());
        return new KeelConfiguration(jsonObject);
    }

    public @Nullable Long readAsLong(String... keychain) {
        String s = readString(keychain);
        return s == null ? null : Long.valueOf(s);
    }

    public @Nullable Integer readAsInteger(String... keychain) {
        String s = readString(keychain);
        return s == null ? null : Integer.valueOf(s);
    }

    /**
     * Parse TRUE/FALSE to boolean ignoring case.
     */
    public @Nullable Boolean readAsBoolean(String... keychain) {
        String s = readString(keychain);
        return s == null ? null : Boolean.valueOf(s);
    }

    @Override
    public @Nonnull JsonObject toJsonObject() {
        return this.data;
    }

    @Override
    public @Nonnull KeelConfiguration reloadDataFromJsonObject(@Nonnull JsonObject data) {
        this.data = data;
        return this;
    }
}
