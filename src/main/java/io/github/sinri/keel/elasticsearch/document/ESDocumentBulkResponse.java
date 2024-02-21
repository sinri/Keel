package io.github.sinri.keel.elasticsearch.document;

import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.vertx.core.json.JsonObject;

/**
 * Hot Fix 3.1.9.1
 */
public class ESDocumentBulkResponse extends SimpleJsonifiableEntity {
    public ESDocumentBulkResponse(JsonObject jsonObject) {
        super(jsonObject);
    }
}
