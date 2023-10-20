package io.github.sinri.keel.elasticsearch.search;

import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.vertx.core.json.JsonObject;

public class ESSearchResponse extends SimpleJsonifiableEntity {
    public ESSearchResponse(JsonObject jsonObject) {
        super(jsonObject);
    }
}
