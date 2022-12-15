package io.github.sinri.keel.core.properties;

import io.github.sinri.keel.lagecy.Keel;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    /**
     * @since 2.8 增加了值有可去空白文字的警告
     */
    public String getProperty(String key) {
        var x = properties.getProperty(key);
        if (x != null) {
            if (x.trim().length() != x.length()) {
                Keel.outputLogger(getClass().getName()).warning("property value might need being trimmed", new JsonObject().put("key", key));
            }
        }
        return x;
    }

    /**
     * @since 2.8 增加了值有可去空白文字的警告
     */
    public String getProperty(String key, String defaultValue) {
        var x = properties.getProperty(key, defaultValue);
        if (x != null && !Objects.equals(x, defaultValue)) {
            if (x.trim().length() != x.length()) {
                Keel.outputLogger(getClass().getName()).warning("property value might need being trimmed", new JsonObject().put("key", key));
            }
        }
        return x;
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
     * @param keyPrefix Prefix of key to filter out, do not add dot to tail
     * @return such as C=X, C.DEF=Y
     * @since 1.10
     * For A.B.C=X, A.B.C.DEF=Y
     * Prefix as A.B
     * Result as C=X, C.DEF=Y
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
     * @param regex REGEX, do not add dot to tail (`[a-z]+\.[0-9]+` would be changed to `^[a-z]+\.[0-9]+`, the dot amongst regex should be escaped)
     * @return Result sub reader
     * For A.B.C=X, A.B.C.DEF=Y
     * Filter Regex = `A\.[A-Z]`
     * Result C=X, C.DEF=Y
     */
    public KeelPropertiesReader filterUsingRegex(String regex) {
        Properties x = new Properties();
        Pattern pattern = Pattern.compile("^" + regex + "\\.");
        properties.forEach((key, value) -> {
            Matcher matcher = pattern.matcher(String.valueOf(key));
            if (matcher.find()) {
                // System.out.println("FILTER "+key+" -> "+value);
                x.put(key.toString().substring(matcher.group().length() + 1), value);
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
        properties.forEach((key, value) -> keys.add(key.toString()));
        return keys;
    }

    /**
     * @return Json Object for the properties
     * @since 1.11
     */
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        Set<String> plainKeySet = getPlainKeySet();
        for (var plainKey : plainKeySet) {
            String[] components = plainKey.split("\\.");
            List<Object> keychain = Arrays.asList(components);
            Keel.helpers().json().writeIntoJsonObject(jsonObject, keychain, getProperty(plainKey));
        }
        return jsonObject;
    }

    /**
     * @param classOfT class of the target class of subclass of KeelOptions
     * @param <T>      the target class of subclass of KeelOptions
     * @return the generated instance of the target class of subclass of KeelOptions
     * @throws RuntimeException when InstantiationException, IllegalAccessException, InvocationTargetException or NoSuchMethodException occurred.
     * @since 1.11
     */
    public <T extends KeelOptions> T toConfiguration(Class<T> classOfT) {
        try {
            T options = classOfT.getConstructor().newInstance();
            options.overwritePropertiesWithJsonObject(this.toJsonObject());
            return options;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
