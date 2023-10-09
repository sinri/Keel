package io.github.sinri.keel.facade;

import io.github.sinri.keel.core.json.JsonifiableEntity;
import io.github.sinri.keel.helper.KeelHelpers;
import io.vertx.core.json.JsonObject;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @since 3.0.0
 */
public class KeelConfiguration implements JsonifiableEntity<KeelConfiguration> {

    private JsonObject data = new JsonObject();

    public KeelConfiguration() {
    }

    public KeelConfiguration(@Nonnull JsonObject jsonObject) {
        this.data = jsonObject;
    }

    public KeelConfiguration(@Nonnull Properties properties) {
        this.data = KeelConfiguration.transformPropertiesToJsonObject(properties);
    }

    static @Nonnull JsonObject transformPropertiesToJsonObject(Properties properties) {
        JsonObject jsonObject = new JsonObject();

        Set<String> plainKeySet = new HashSet<>();
        properties.forEach((key, value) -> plainKeySet.add(key.toString()));

        for (var plainKey : plainKeySet) {
            String[] components = plainKey.split("\\.");
            List<Object> keychain = Arrays.asList(components);
            KeelHelpers.jsonHelper()
                    .writeIntoJsonObject(jsonObject, keychain, properties.getProperty(plainKey));
        }
        return jsonObject;
    }

    static KeelConfiguration createFromPropertiesFile(String propertiesFileName) {
        KeelConfiguration p = new KeelConfiguration();
        p.loadPropertiesFile(propertiesFileName);
        return p;
    }

    static KeelConfiguration createFromProperties(Properties properties) {
        KeelConfiguration p = new KeelConfiguration();
        p.putAll(properties);
        return p;
    }

    static KeelConfiguration createFromJsonObject(JsonObject jsonObject) {
        KeelConfiguration p = new KeelConfiguration();
        p.putAll(jsonObject);
        return p;
    }

    public KeelConfiguration putAll(KeelConfiguration keelConfiguration) {
        return putAll(keelConfiguration.toJsonObject());
    }

    public KeelConfiguration putAll(Properties properties) {
        return putAll(KeelConfiguration.transformPropertiesToJsonObject(properties));
    }

    public KeelConfiguration putAll(JsonObject jsonObject) {
        data.mergeIn(jsonObject);
        return this;
    }

    /**
     * @since 3.0.1
     */
    public KeelConfiguration loadPropertiesFile(String propertiesFileName) {
        return loadPropertiesFile(propertiesFileName, StandardCharsets.UTF_8);
    }

    public KeelConfiguration loadPropertiesFile(String propertiesFileName, Charset charset) {
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
    public KeelConfiguration loadPropertiesFileContent(String content) {
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(content));
        } catch (IOException e) {
            throw new RuntimeException("Cannot load given properties content.", e);
        }
        return putAll(properties);
    }

    public KeelConfiguration extract(String... keychain) {
        JsonObject jsonObject = Objects.requireNonNullElse(readJsonObject(keychain), new JsonObject());
        return new KeelConfiguration(jsonObject);
    }

    /**
     * @param dotJoinedKeyChain raw keychain in properties file, such as `a.b.c`
     * @since 3.0.1
     */
    @Deprecated(since = "3.0.1")
    public String fastRead(@NotNull String dotJoinedKeyChain) {
        String[] split = dotJoinedKeyChain.split("\\.");
        return readString(split);
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
    public @NotNull JsonObject toJsonObject() {
        return this.data;
    }

    @Override
    public @NotNull KeelConfiguration reloadDataFromJsonObject(JsonObject data) {
        this.data = data;
        return this;
    }
}
