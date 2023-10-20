package io.github.sinri.keel.elasticsearch.document;

import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.vertx.core.json.JsonObject;

public class ESDocumentDeleteResponse extends SimpleJsonifiableEntity {
    public ESDocumentDeleteResponse(JsonObject jsonObject) {
        super(jsonObject);
    }

    // TODO
    /*
    {
  "_shards": {
    "total": 2,
    "failed": 0,
    "successful": 2
  },
  "_index": "my-index-000001",
  "_id": "1",
  "_version": 2,
  "_primary_term": 1,
  "_seq_no": 5,
  "result": "deleted"
}
     */
}
