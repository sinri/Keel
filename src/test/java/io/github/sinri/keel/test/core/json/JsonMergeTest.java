package io.github.sinri.keel.test.core.json;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonMergeTest {
    public static void main(String[] args) {
        JsonObject a = new JsonObject()
                .put("a", "A")
                .put("b", new JsonObject()
                        .put("c1", "C1")
                        .put("c2", "C2")
                        .put("d", new JsonObject()
                                .put("e", "E")
                        )
                )
                .put("f", new JsonArray()
                        .add("g")
                        .add("h")
                );
        System.out.println(a);

        JsonObject b = new JsonObject()
                .put("a", "+A")
                .put("b", new JsonObject()
                        .put("c1", "+C1")
                        .put("c3", "+C3")
                        .put("d", new JsonArray().add("+G"))
                )
                .put("f", new JsonArray()
                        .add("+J")
                )
                .put("i", "+I")// a里没有，b里有
                ;


        JsonObject shallowMerged = new JsonObject(a.toString()).mergeIn(b);// i.e. a.mergeIn(b,false); or a.mergeIn(b,1);
        System.out.println(shallowMerged);
        JsonObject deepMerged = new JsonObject(a.toString()).mergeIn(b, true);// i.e. a.mergeIn(b,MAX_INT);
        System.out.println(deepMerged);

    }
}
