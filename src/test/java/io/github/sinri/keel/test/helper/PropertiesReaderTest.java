package io.github.sinri.keel.test.helper;

import io.github.sinri.keel.core.properties.KeelOptions;
import io.github.sinri.keel.core.properties.KeelPropertiesReader;
import io.vertx.core.json.JsonObject;

public class PropertiesReaderTest {
    public static void main(String[] args) {
        KeelPropertiesReader keelPropertiesReader = KeelPropertiesReader.loadReaderWithFile("test.sample.properties");
        System.out.println("mysql.default_data_source_name -> " + keelPropertiesReader.getProperty("mysql.default_data_source_name"));

        System.out.println("test.array1=" + keelPropertiesReader.getProperty("test.array1"));
        System.out.println("test.array1[1]=" + keelPropertiesReader.getProperty("test.array1[1]"));

        //System.out.println(keelPropertiesReader.computeKeyTree().toJsonObject(keelPropertiesReader).toString());

        KeelPropertiesReader keelPropertiesReader1 = keelPropertiesReader.filter("mysql");

        System.out.println("default_data_source_name -> " + keelPropertiesReader1.getProperty("default_data_source_name"));
        System.out.println("local.host -> " + keelPropertiesReader1.getProperty("local.host"));

        TestPojo testPojo = keelPropertiesReader.filter("test.pojo").toConfiguration(TestPojo.class);
        System.out.println(testPojo);
    }

    public static class TestPojo extends KeelOptions {
        public boolean booleanField;
        public byte byteField;
        public short shortField;
        public int intField;
        public long LongField;
        public float floatField;
        public double doubleField;
        public String stringField;

        public JsonObjectChild jsonObjectField;

        public TestPojo(JsonObject jsonObject) {
            super(jsonObject);
            System.out.println("TestPojo constructor: " + jsonObject.toString());
        }

        @Override
        protected void initializeProperties() {

        }

        public String toString() {
            return "booleanField=" + booleanField + " " +
                    "byteField=" + byteField + " " +
                    "shortField=" + shortField + " " +
                    "intField=" + intField + " " +
                    "LongField=" + LongField + " " +
                    "floatField=" + floatField + " " +
                    "doubleField=" + doubleField + " " +
                    "stringField=" + stringField + " " +
                    "jsonObjectField=" + jsonObjectField.toString() + " !";
        }

        public static class JsonObjectChild extends KeelOptions {

            public String a;
            public String b;

            public JsonObjectChild(JsonObject jsonObject) {
                super(jsonObject);
            }

            @Override
            protected void initializeProperties() {

            }

            public String toString() {
                return "a=" + a + " b=" + b;
            }
        }
    }
}
