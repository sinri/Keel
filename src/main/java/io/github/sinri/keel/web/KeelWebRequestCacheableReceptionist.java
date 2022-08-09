package io.github.sinri.keel.web;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.cache.KeelCacheInterface;
import io.github.sinri.keel.cache.impl.KeelCacheAlef;
import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

/**
 * @since 2.6
 */
@Deprecated(since = "2.8")
abstract public class KeelWebRequestCacheableReceptionist extends KeelWebRequestReceptionist {
    public static final String FIELD_CACHE_KEY_MD5 = "cache_key_md5";
    public static final String FIELD_USE_CACHE = "used_cache";
    private static final KeelCacheInterface<String, Object> cacheInstance = new KeelCacheAlef<>();

    public KeelWebRequestCacheableReceptionist(RoutingContext routingContext) {
        super(routingContext);
    }

    protected KeelLogger getCacheLogger() {
        return KeelLogger.silentLogger();
    }

    /**
     * @since 2.6
     */
    abstract protected String getRequestDigestAsCacheKey();

    abstract protected long getLifeInSeconds();

    @Override
    protected Future<Object> handlerForFiltersPassed() {
        String key = getRequestDigestAsCacheKey();

        if (key == null) {
            return this.dealWithRequest();
        }

        String md5_of_key = Keel.stringHelper().md5(key);
        this.getRoutingContext().put(FIELD_CACHE_KEY_MD5, md5_of_key);
        Object cached = cacheInstance.read(key);
        if (cached == null) {
            this.getRoutingContext().put(FIELD_USE_CACHE, false);
            return this.dealWithRequest()
                    .compose(result -> {
                        getCacheLogger().notice("CACHING", new JsonObject()
                                .put("md5", md5_of_key)
                                .put("raw", key)
                        );
                        cacheInstance.save(key, result, getLifeInSeconds());
                        return Future.succeededFuture(result);
                    });
        } else {
            getCacheLogger().notice("CACHED", new JsonObject()
                    .put("md5", md5_of_key)
            );
            this.getRoutingContext().put(FIELD_USE_CACHE, true);
            return Future.succeededFuture(cached);
        }
    }
}
