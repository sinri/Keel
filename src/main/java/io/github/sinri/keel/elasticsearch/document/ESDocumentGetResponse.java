package io.github.sinri.keel.elasticsearch.document;

import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.vertx.core.json.JsonObject;

public class ESDocumentGetResponse extends SimpleJsonifiableEntity {
    public ESDocumentGetResponse(JsonObject jsonObject) {
        super(jsonObject);
    }

    // TODO

    /*
    {
  "_index": "my-index-000001",
  "_id": "0",
  "_version": 1,
  "_seq_no": 0,
  "_primary_term": 1,
  "found": true,
  "_source": {
    "@timestamp": "2099-11-15T14:12:12",
    "http": {
      "request": {
        "method": "get"
      },
      "response": {
        "status_code": 200,
        "bytes": 1070000
      },
      "version": "1.1"
    },
    "source": {
      "ip": "127.0.0.1"
    },
    "message": "GET /search HTTP/1.1 200 1070000",
    "user": {
      "id": "kimchy"
    }
  }
}
     */
}
