package io.github.sinri.keel.elasticsearch.index;

import io.github.sinri.keel.core.json.SimpleJsonifiableEntity;
import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class ESIndexGetResponse extends SimpleJsonifiableEntity {
    public ESIndexGetResponse(JsonObject jsonObject) {
        super(jsonObject);
    }

    public Map<String, ESIndexMeta> indexMetaMap() {
        Map<String, ESIndexMeta> map = new HashMap<>();
        this.toJsonObject().forEach(entry -> {
            map.put(entry.getKey(), new ESIndexMeta((JsonObject) entry.getValue()));
        });
        return map;
    }

    public static class ESIndexMeta extends SimpleJsonifiableEntity {
        public ESIndexMeta(JsonObject jsonObject) {
            super(jsonObject);
        }

        // todo
    }

    /*
    {
        "kumori-es-test-1":{
            "aliases":{},
            "mappings":{
                "properties":{
                    "abstractText":{
                        "type":"text",
                        "fields":{
                            "keyword":{
                                "type":"keyword",
                                "ignore_above":256
                            }
                        }
                    },
                    "fullText":{
                        "type":"text",
                        "fields":{
                            "keyword":{
                                "type":"keyword",
                                "ignore_above":256
                            }
                        }
                    },
                    "id":{
                        "type":"text",
                        "fields":{
                            "keyword":{
                                "type":"keyword",
                                "ignore_above":256
                            }
                        }
                    },
                    "title":{
                        "type":"text",
                        "fields":{
                            "keyword":{
                                "type":"keyword",
                                "ignore_above":256
                            }
                        }
                    }
                }
            },
            "settings":{
                "index":{
                    "refresh_interval":null,
                    "indexing":{
                        "slowlog":{
                            "threshold":{
                                "index":{
                                    "warn":"200ms",
                                    "trace":"20ms",
                                    "debug":"50ms",
                                    "info":"100ms"
                                }
                            },
                            "source":"1000"
                        }
                    },
                    "translog":{
                        "sync_interval":null,
                        "durability":null
                    },
                    "provided_name":"kumori-es-test-1",
                    "creation_date":"1697428874746",
                    "unassigned":{
                        "node_left":{
                            "delayed_timeout":"5m"
                        }
                    },
                    "number_of_replicas":"1",
                    "uuid":"CKA9AOQ3RgC3hESqFH7TOA",
                    "version":{
                        "created":"8090199"
                    },
                    "routing":{
                        "allocation":{
                            "include":{
                                "_tier_preference":"data_content"
                            }
                        }
                    },
                    "search":{
                        "slowlog":{
                            "threshold":{
                                "fetch":{
                                    "warn":"200ms",
                                    "trace":"50ms",
                                    "debug":"80ms",
                                    "info":"100ms"
                                },
                                "query":{
                                    "warn":"200ms",
                                    "trace":"50ms",
                                    "debug":"80ms",
                                    "info":"100ms"
                                }
                            }
                        }
                    },
                    "number_of_shards":"1",
                    "merge":{
                        "policy":{
                            "segments_per_tier":null,
                            "max_merged_segment":null
                        }
                    }
                }
            }
        }
    }
     */
}
