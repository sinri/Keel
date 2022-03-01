package io.github.sinri.keel.test.helper.json;

import io.vertx.core.json.JsonObject;

public class POJOTest {
    public static void main(String[] args) {
        Pojo pojo = new Pojo("a", "b", "c");
        JsonObject jsonObject = JsonObject.mapFrom(pojo);
        System.out.println(jsonObject);
    }
}
