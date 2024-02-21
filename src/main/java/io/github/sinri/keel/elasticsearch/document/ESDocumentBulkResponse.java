package io.github.sinri.keel.elasticsearch.document;

import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.vertx.core.json.JsonObject;

/**
 * @since 3.1.10
 */
public class ESDocumentBulkResponse extends SimpleJsonifiableEntity {
    public ESDocumentBulkResponse(JsonObject jsonObject) {
        super(jsonObject);
    }
}
