package io.github.sinri.keel.test.core.json;

import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.vertx.core.json.JsonObject;

public class JsonReaderTypeTest {
    public static void main(String[] args) {
        String s = "{\"n\":\"123\"}";
        JsonObject jsonObject = new JsonObject(s);
        /*
        Integer n =jsonObject.getInteger("n");
        // Exception in thread "main" java.lang.ClassCastException: class java.lang.String cannot be cast to class java.lang.Number (java.lang.String and java.lang.Number are in module java.base of loader 'bootstrap')
        //	at io.vertx.core.json.JsonObject.getInteger(JsonObject.java:168)
        //	at io.github.sinri.keel.test.core.json.JsonReaderTypeTest.main(JsonReaderTypeTest.java:9)
         */
        B1 b1 = new B1();
        b1.reloadDataFromJsonObject(jsonObject);
        Integer n = b1.readInteger("n");
        System.out.println(n);
    }

    static class B1 extends SimpleJsonifiableEntity {

    }
}
