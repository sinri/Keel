package io.github.sinri.keel.test.v1.helper.json;

import io.github.sinri.keel.core.json.JsonifiableEntity;
import io.vertx.core.json.JsonObject;

public class JsonEntity implements JsonifiableEntity<JsonEntity> {
    public String a;
    protected Integer b;
    private boolean c;

    public static void main(String[] args) {
        JsonEntity jsonEntity = new JsonEntity();
        jsonEntity.a = "A";
        jsonEntity.b = 1;
        jsonEntity.c = false;

        JsonObject jsonObject = jsonEntity.toJsonObject();
        System.out.println(jsonObject.toString());
        jsonEntity.a = "B";
        jsonEntity.b = 2;
        jsonEntity.c = true;
        System.out.println(jsonEntity.reloadDataFromJsonObject(jsonObject));
    }

    @Override
    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("a", a)
                .put("b", b)
                .put("c", c);
    }

    @Override
    public JsonEntity reloadDataFromJsonObject(JsonObject jsonObject) {
        this.a = jsonObject.getString("a");
        this.b = jsonObject.getInteger("b");
        this.c = jsonObject.getBoolean("c");
        return this;
    }

    @Override
    public String toString() {
        return "JsonEntity{" +
                "a='" + a + '\'' +
                ", b=" + b +
                ", c=" + c +
                '}';
    }
}
