package io.github.sinri.keel.elasticsearch.index;

import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.vertx.core.json.JsonObject;

public class ESIndexDeleteResponse extends SimpleJsonifiableEntity {
    public ESIndexDeleteResponse(JsonObject jsonObject) {
        super(jsonObject);
    }
}
