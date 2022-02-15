package io.github.sinri.keel.test.helper;

import io.github.sinri.keel.core.properties.KeelPropertiesReader;

public class PropertiesReaderTest {
    public static void main(String[] args) {
        KeelPropertiesReader keelPropertiesReader = KeelPropertiesReader.loadReaderWithFile("test.sample.properties");
        System.out.println("mysql.default_data_source_name -> " + keelPropertiesReader.getProperty("mysql.default_data_source_name"));


        KeelPropertiesReader keelPropertiesReader1 = keelPropertiesReader.filter("mysql");

        System.out.println("default_data_source_name -> " + keelPropertiesReader1.getProperty("default_data_source_name"));
        System.out.println("local.host -> " + keelPropertiesReader1.getProperty("local.host"));

    }
}
