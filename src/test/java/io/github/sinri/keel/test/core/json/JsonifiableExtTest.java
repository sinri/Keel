package io.github.sinri.keel.test.core.json;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.json.JsonObject;

public class JsonifiableExtTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v -> {
            J1 j1 = new J1()
                    .setA("aaaa")
                    .setB(new JsonObject()
                            .put("aaaa", "bbbbb")
                    );
            System.out.println(j1);

            j1.forEach(entry -> {
                System.out.println(entry.getKey() + " -> " + entry.getValue());
            });

            Keel.getVertx().eventBus().consumer("consumer", message -> {
                System.out.println("consumer received message: " + message.body() + " C: " + ((J1) message.body()).getC());
            });
            Keel.getVertx().eventBus().publish("consumer", j1);

        });


    }

    public static class J1 extends SimpleJsonifiableEntity {
        public String getA() {
            return readString("a");
        }

        public J1 setA(String a) {
            this.jsonObject.put("a", a);
            return this;
        }

        public JsonObject getB() {
            return readJsonObject("b");
        }

        public J1 setB(JsonObject b) {
            this.jsonObject.put("b", b);
            return this;
        }

        public String getC() {
            return getB().getString(getA());
        }
    }
}
