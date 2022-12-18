package io.github.sinri.keel.facade;

import io.github.sinri.keel.core.json.JsonifiableEntity;
import io.github.sinri.keel.helper.KeelHelpers;
import io.vertx.core.json.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @since 3.0.0
 */
public interface KeelConfiguration extends JsonifiableEntity<KeelConfiguration> {

    static KeelConfiguration createFromPropertiesFile(String propertiesFileName) {
        KeelConfigurationImpl p = new KeelConfigurationImpl();
        p.loadPropertiesFile(propertiesFileName);
        return p;
    }

    static KeelConfiguration createFromProperties(Properties properties) {
        KeelConfigurationImpl p = new KeelConfigurationImpl();
        p.putAll(properties);
        return p;
    }

    static KeelConfiguration createFromJsonObject(JsonObject jsonObject) {
        KeelConfigurationImpl p = new KeelConfigurationImpl();
        p.putAll(jsonObject);
        return p;
    }

    static @Nonnull JsonObject transformPropertiesToJsonObject(Properties properties) {
        JsonObject jsonObject = new JsonObject();

        Set<String> plainKeySet = new HashSet<>();
        properties.forEach((key, value) -> plainKeySet.add(key.toString()));

        for (var plainKey : plainKeySet) {
            String[] components = plainKey.split("\\.");
            List<Object> keychain = Arrays.asList(components);
            KeelHelpers.getInstance().jsonHelper()
                    .writeIntoJsonObject(jsonObject, keychain, properties.getProperty(plainKey));
        }
        return jsonObject;
    }

    @Deprecated
    static void main(String[] args) {
        KeelConfiguration a = KeelConfiguration.createFromJsonObject(new JsonObject()
                .put("a", "b")
                .put("b", new JsonObject()
                        .put("b1", "asdasd")
                        .put("b2", "y89has")
                        .put("c", new JsonObject()
                                .put("c1", "asfdsf")
                                .put("c2", "asdasdasd")
                        )
                )
        );
        System.out.println("a: " + a.toJsonObject());
        KeelConfiguration b = a.extract("b");
        System.out.println("b: " + b.toJsonObject());
        KeelConfiguration c = a.extract("b", "c");
        System.out.println("c: " + c.toJsonObject());
    }

    default KeelConfiguration putAll(KeelConfiguration keelConfiguration) {
        return putAll(keelConfiguration.toJsonObject());
    }

    default KeelConfiguration putAll(Properties properties) {
        return putAll(KeelConfiguration.transformPropertiesToJsonObject(properties));
    }

    KeelConfiguration putAll(JsonObject jsonObject);

    default KeelConfiguration loadPropertiesFile(String propertiesFileName) {
        Properties properties = new Properties();
        try {
            // here, the file named as `propertiesFileName` should be put along with JAR
            properties.load(new FileReader(propertiesFileName));
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

    default @Nullable KeelConfiguration extract(String... keychain) {
        return readJsonifiableEntity(KeelConfigurationImpl.class, keychain);
    }
}
