package io.github.sinri.keel.test.core.json;

import io.github.sinri.keel.core.json.JsonifiableEntity;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonBoxTest {
    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v -> {
            Box1 box1 = new Box1(
                    new JsonObject()
                            .put("string_value_1", "A")
                            .put("long_value_2", 123458724)
                            .put("float_value_3", 123.556f)
                            .put("boolean_value_4", false)
                            .put("json_object_5", new JsonObject()
                                    .put("a", "123")
                                    .put("b", 34)
                            )
                            .put("json_array_6", new JsonArray()
                                    .add(124235)
                            )
            );
            System.out.println(box1.getStringValue1());
            System.out.println(box1.getLongValue2());
            System.out.println(box1.getFloatValue3());
            System.out.println(box1.getBooleanValue4());
            System.out.println(box1.getNestedStringA());
            System.out.println(box1.getJsonArray6());
            System.out.println(box1.getNestedIntegerB());
            System.out.println(box1.getBox2().getB());
        });


    }

    public static class Box1 implements JsonifiableEntity<Box1> {
        private JsonObject jsonObject;

        public Box1(JsonObject jsonObject) {
            super();
            this.reloadDataFromJsonObject(jsonObject);
        }

        public String getStringValue1() {
            return readString("string_value_1");
        }

        public Long getLongValue2() {
            return this.readLong("long_value_2");
        }

        public Float getFloatValue3() {
            return this.readFloat("float_value_3");
        }

        public Boolean getBooleanValue4() {
            return this.readBoolean("boolean_value_4");
        }

        public JsonObject getJsonObject5() {
            return this.readJsonObject("json_object_5");
        }

        public String getNestedStringA() {
            return this.readString("json_object_5", "a");
        }

        public JsonArray getJsonArray6() {
            return this.readJsonArray("json_array_6");
        }

        public Integer getNestedIntegerB() {
            return this.readInteger("json_array_6", "0");
        }

        public Box2 getBox2() {
            return this.readJsonifiableEntity(Box2.class, "json_object_5");
        }

        @Override
        public JsonObject toJsonObject() {
            return this.jsonObject;
        }

        @Override
        public Box1 reloadDataFromJsonObject(JsonObject jsonObject) {
            this.jsonObject = jsonObject;
            return this;
        }
    }

    public static class Box2 implements JsonifiableEntity<Box2> {
        private JsonObject jsonObject;

        public Box2(JsonObject jsonObject) {
            super();
            this.reloadDataFromJsonObject(jsonObject);
        }

        public String getA() {
            return readString("a");
        }

        public Integer getB() {
            return readInteger("b");
        }

        @Override
        public JsonObject toJsonObject() {
            return this.jsonObject;
        }

        @Override
        public Box2 reloadDataFromJsonObject(JsonObject jsonObject) {
            this.jsonObject = jsonObject;
            return this;
        }
    }
}
