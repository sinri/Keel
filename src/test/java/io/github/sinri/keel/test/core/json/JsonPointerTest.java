package io.github.sinri.keel.test.core.json;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.github.sinri.keel.test.SharedTestBootstrap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.pointer.JsonPointer;
import io.vertx.core.json.pointer.JsonPointerIterator;

import java.util.List;

public class JsonPointerTest {

    private static <T> T dig(JsonObject jsonObject, JsonPointer jsonPointer, Class<T> classOfT) {
        Object result = jsonPointer.queryJson(jsonObject);
        return classOfT.cast(result);
    }

    public static void main(String[] args) {
        SharedTestBootstrap.initialize(v -> {
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
                    .put("a1", new JsonObject()
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

            JsonPointer p1 = JsonPointer.create().append("a").append(0).append("b");
            Object x = p1.writeJson(
                    new JsonObject()
                            .put("a", new JsonArray()
                                    .add(new JsonObject()
                                            .put("b", "B")
                                    )
                            ),
                    "X"
            );
            System.out.println(x.getClass().getName() + " : " + x);

            KeelLogger logger = Keel.outputLogger("y");

            Object y = p1.write(
                    new JsonObject(),
                    new JsonPointerIterator() {
                        @Override
                        public boolean isObject(@io.vertx.codegen.annotations.Nullable Object currentValue) {
                            var result = currentValue instanceof JsonObject;
                            logger.info("isObject", new JsonObject()
                                    .put("currentValue", currentValue)
                                    .put("result", result)
                            );
                            return result;
                        }

                        @Override
                        public boolean isArray(@io.vertx.codegen.annotations.Nullable Object currentValue) {
                            var result = (currentValue instanceof JsonArray);
                            logger.info("isArray", new JsonObject()
                                    .put("currentValue", currentValue)
                                    .put("result", result)
                            );
                            return result;
                        }

                        @Override
                        public boolean isNull(@io.vertx.codegen.annotations.Nullable Object currentValue) {
                            var result = currentValue == null;
                            logger.info("isNull", new JsonObject()
                                    .put("currentValue", currentValue)
                                    .put("result", result)
                            );
                            return result;
                        }

                        @Override
                        public boolean objectContainsKey(@io.vertx.codegen.annotations.Nullable Object currentValue, String key) {
                            boolean result = false;
                            if (currentValue instanceof JsonObject) {
                                result = jsonObject.containsKey(key);
                            }
                            logger.info("objectContainsKey", new JsonObject()
                                    .put("currentValue", currentValue)
                                    .put("result", result)
                            );
                            return result;
                        }

                        @Override
                        public Object getObjectParameter(@io.vertx.codegen.annotations.Nullable Object currentValue, String key, boolean createOnMissing) {
                            Object result = null;
                            if (currentValue instanceof JsonObject) {
                                if (((JsonObject) currentValue).containsKey(key)) {
                                    result = ((JsonObject) currentValue).getValue(key);
                                } else if (createOnMissing) {
                                    result = new JsonObject();
                                    ((JsonObject) currentValue).put(key, result);
                                }
                            }
                            logger.info("getObjectParameter", new JsonObject()
                                    .put("currentValue", currentValue)
                                    .put("key", key)
                                    .put("createOnMissing", createOnMissing)
                                    .put("result", result)
                            );

                            return result;
                        }

                        @Override
                        public Object getArrayElement(@io.vertx.codegen.annotations.Nullable Object currentValue, int i) {
                            Object result = null;
                            if (currentValue instanceof JsonArray) {
                                result = ((JsonArray) currentValue).getValue(i);
                            }
                            logger.info("getArrayElement", new JsonObject()
                                    .put("currentValue", currentValue)
                                    .put("i", i)
                                    .put("result", result)
                            );
                            return result;
                        }

                        @Override
                        public boolean writeObjectParameter(@io.vertx.codegen.annotations.Nullable Object currentValue, String key, @io.vertx.codegen.annotations.Nullable Object value) {
                            boolean result = false;
                            if (currentValue instanceof JsonObject) {
                                ((JsonObject) currentValue).put(key, value);
                                result = true;
                            }
                            logger.info("writeObjectParameter", new JsonObject()
                                    .put("currentValue", currentValue)
                                    .put("key", key)
                                    .put("value", value)
                                    .put("result", result)
                            );
                            return result;
                        }

                        @Override
                        public boolean writeArrayElement(@io.vertx.codegen.annotations.Nullable Object currentValue, int i, @io.vertx.codegen.annotations.Nullable Object value) {
                            boolean result = false;
                            if (currentValue instanceof JsonArray) {
                                ((JsonArray) currentValue).add(i, value);
                                result = true;
                            }
                            logger.info("writeArrayElement", new JsonObject()
                                    .put("currentValue", currentValue)
                                    .put("i", i)
                                    .put("value", value)
                                    .put("result", result)
                            );
                            return result;
                        }

                        @Override
                        public boolean appendArrayElement(@io.vertx.codegen.annotations.Nullable Object currentValue, @io.vertx.codegen.annotations.Nullable Object value) {
                            boolean result = false;
                            if (currentValue instanceof JsonArray) {
                                ((JsonArray) currentValue).add(value);
                                result = true;
                            }
                            logger.info("appendArrayElement", new JsonObject()
                                    .put("currentValue", currentValue)
                                    .put("value", value)
                                    .put("result", result)
                            );
                            return result;
                        }
                    },
                    "Y",
                    true
            );
            System.out.println(y.getClass().getName() + " : " + y);

        });


    }
}
