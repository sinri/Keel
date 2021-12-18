package io.github.sinri.Keel.core.properties;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class KeelPropertiesReader {

    protected Properties properties;

    public KeelPropertiesReader() {
        properties = new Properties();
    }

    public static KeelPropertiesReader loadReaderWithFile(String propertiesFileName) {
        KeelPropertiesReader reader = new KeelPropertiesReader();
        try {
            // here, the file named as `propertiesFileName` should be put along with JAR
            File configPropertiesFile = new File(propertiesFileName);
            reader.properties.load(new FileReader(configPropertiesFile));
        } catch (IOException e) {
            Logger.getLogger("KeelPropertiesReader").warning("Cannot find the file config.properties. Use the embedded one.");
            try {
                reader.properties.load(KeelPropertiesReader.class.getClassLoader().getResourceAsStream(propertiesFileName));
                // System.out.println("reader.properties -> "+reader.properties);
            } catch (IOException ex) {
                throw new RuntimeException("Cannot find the embedded file config.properties.", ex);
            }
        }
        //readerMap.put(readerName, reader);
        return reader;
    }

    public void appendPropertiesFromFile(String propertiesFileName) {
        try {
            // here, the file named as `propertiesFileName` should be put along with JAR
            File configPropertiesFile = new File(propertiesFileName);
            properties.load(new FileReader(configPropertiesFile));
        } catch (IOException e) {
            Logger.getLogger("KeelPropertiesReader").warning("Cannot find the file config.properties. Use the embedded one.");
            try {
                properties.load(KeelPropertiesReader.class.getClassLoader().getResourceAsStream(propertiesFileName));
                // System.out.println("reader.properties -> "+reader.properties);
            } catch (IOException ex) {
                throw new RuntimeException("Cannot find the embedded file config.properties.", ex);
            }
        }
    }

//    public static KeelPropertiesReader getReader(String readerName) {
//        return readerMap.get(readerName);
//    }

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
}
