package io.github.sinri.keel.test.core.json;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.json.scheme.*;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class SchemeTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize();

        test_o_1();
    }

    private static void test_o_1() {
        JsonObjectScheme jsonObjectScheme = new JsonObjectScheme()
                .setElementSchemeForKey("a", new JsonValueScheme())
                .setElementSchemeForKey("b", new JsonObjectScheme()
                        .setElementSchemeForKey("b1", new JsonNumberScheme())
                        .setElementSchemeForKey("b2", new JsonStringScheme())
                        .setElementSchemeForKey("b3", new JsonBooleanScheme()
                                .setExpected(false))
                        .setElementSchemeForKey("b4", new JsonNullScheme())
                )
                .setElementSchemeForKey("c", new JsonArrayScheme()
                        .setDefaultElementScheme(new JsonValueScheme())
                );
        var result = jsonObjectScheme.validate(new JsonObject()
                .put("a", "a")
                .put("b", new JsonObject()
                        .put("b1", 1)
                        .put("b2", "1")
                        .put("b3", false)
                        .put("b4", null)
                )
                .put("c", new JsonArray()
                        .add("c1")
                )
        );
        Keel.outputLogger("test_o_1").info("result: " + result);
    }
}
