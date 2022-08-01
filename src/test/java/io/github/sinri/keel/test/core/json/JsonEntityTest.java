package io.github.sinri.keel.test.core.json;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class JsonEntityTest {
    public static void main(String[] args) {
        JsonObject x = new JsonObject()
                .put("formId", 1)
                .put("formCode", 2)
                .put("formBody", 3);

        RequestBody requestBody = new RequestBody(x);
        boolean validated = requestBody.validate();
        System.out.println("validated: " + validated);
    }

    public static class RequestBody extends EntityWithJsonObject {

        public RequestBody(JsonObject jsonObject) {
            super(jsonObject);
        }

        public Long formId() {
            return this.readLong("formId");
        }

        public String formCode() {
            return this.readString("formCode");
        }

        public FormBody formBody() {
            return this.read("formBody", FormBody.class);
        }


    }

    public static class FormBody extends EntityWithJsonObject {

        public FormBody(JsonObject jsonObject) {
            super(jsonObject);
        }

        public String question1() {
            return this.readString("question1");
        }

        public Double question2() {
            return this.readDouble("question2");
        }

        public JsonArray question3() {
            return this.readJsonArray("question3");
        }

        public FormExtParamArray question4() {
            return this.read("question4", FormExtParamArray.class);
        }
    }

    public static class FormExtParamArray extends EntityWithJsonArray {

        public FormExtParamArray(JsonArray jsonArray) {
            super(jsonArray);
        }
    }
}
