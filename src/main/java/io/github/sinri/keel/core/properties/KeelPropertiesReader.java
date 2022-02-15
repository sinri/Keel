package io.github.sinri.keel.core.properties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
//import java.util.logging.Logger; // ->   (程序包 java.util.logging 已在模块 java.logging 中声明, 但模块 com.fasterxml.jackson.core 未读取它)

/**
 * Reader for Properties Files used in Keel Project
 */
public class KeelPropertiesReader {

    protected final Properties properties;

    public KeelPropertiesReader() {
        properties = new Properties();
    }

    /**
     * @param properties the raw Properties instance
     * @since 1.10
     */
    public KeelPropertiesReader(Properties properties) {
        this.properties = properties;
    }

    public static KeelPropertiesReader loadReaderWithFile(String propertiesFileName) {
        KeelPropertiesReader reader = new KeelPropertiesReader();
        try {
            // here, the file named as `propertiesFileName` should be put along with JAR
            File configPropertiesFile = new File(propertiesFileName);
            reader.properties.load(new FileReader(configPropertiesFile));
        } catch (IOException e) {
            System.err.println("Cannot find the file config.properties. Use the embedded one.");
            try {
                reader.properties.load(KeelPropertiesReader.class.getClassLoader().getResourceAsStream(propertiesFileName));
            } catch (IOException ex) {
                throw new RuntimeException("Cannot find the embedded file config.properties.", ex);
            }
        }
        return reader;
    }

    public void appendPropertiesFromFile(String propertiesFileName) {
        try {
            // here, the file named as `propertiesFileName` should be put along with JAR
            File configPropertiesFile = new File(propertiesFileName);
            properties.load(new FileReader(configPropertiesFile));
        } catch (IOException e) {
            System.err.println("Cannot find the file config.properties. Use the embedded one.");
            try {
                properties.load(KeelPropertiesReader.class.getClassLoader().getResourceAsStream(propertiesFileName));
            } catch (IOException ex) {
                throw new RuntimeException("Cannot find the embedded file config.properties.", ex);
            }
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public String getProperty(List<String> keys) {
        StringBuilder key = new StringBuilder();
        for (String x : keys) {
            key.append(".").append(x);
        }
        key.deleteCharAt(0);
        return properties.getProperty(key.toString());
    }

    public String getProperty(List<String> keys, String defaultValue) {
        StringBuilder key = new StringBuilder();
        for (String x : keys) {
            key.append(".").append(x);
        }
        key.deleteCharAt(0);
        return properties.getProperty(key.toString(), defaultValue);
    }

    /**
     * @param keyPrefix such as A.B
     * @return such as C=X, C.DEF=Y
     * @since 1.10
     * For A.B.C=X, A.B.C.DEF=Y
     */
    public KeelPropertiesReader filter(String keyPrefix) {
        Properties x = new Properties();
        properties.forEach((key, value) -> {
            if (key.toString().startsWith(keyPrefix + ".")) {
                // System.out.println("FILTER "+key+" -> "+value);
                x.put(key.toString().substring(keyPrefix.length() + 1), value);
            }
        });
        return new KeelPropertiesReader(x);
    }

    /**
     * @return the key set
     * @since 1.10
     */
    public Set<String> getPlainKeySet() {
        Set<String> keys = new HashSet<>();
        properties.forEach((key, value) -> {
            keys.add(key.toString());
        });
        return keys;
    }
}
