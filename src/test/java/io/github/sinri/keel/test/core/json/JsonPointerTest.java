package io.github.sinri.keel.test.core.json;

import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;

import java.util.List;

public class JsonPointerTest {

    private static <T> T dig(JsonObject jsonObject, JsonPointer jsonPointer, Class<T> classOfT) {
        Object result = jsonPointer.queryJson(jsonObject);
        return classOfT.cast(result);
    }

    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        JsonPointer tinyPointer = JsonPointer.create()
                .append("x")
                .append("y");
        JsonPointer pointer = JsonPointer.create()
                .append("a")
                .append("b")
                .append(0)
                .append(List.of("c", "d"))
                .append(tinyPointer);

        JsonObject jsonObject = new JsonObject()
                .put("a", new JsonObject()
                        .put("b", new JsonArray()
                                .add(new JsonObject()
                                        .put("c", new JsonObject()
                                                .put("d", new JsonObject()
                                                        .put("x", new JsonObject()
                                                                .put("y", "Y")
                                                        )
                                                )
                                        )
                                )
                        )
                );

        Object o = pointer.queryJson(jsonObject);
        System.out.println(o);

        System.out.println(dig(jsonObject, pointer, String.class));
    }
}
