package io.github.sinri.keel.redis.mixin;

public interface RedisOrderedSetMixin extends RedisApiMixin {
    // TODO
    //  BZPOPMAX key [key ...] timeout
    //  BZPOPMIN key [key ...] timeout
    //  ZADD key [NX|XX] [GT|LT] [CH] [INCR] score member [score member ...]
    //  ZCARD key
    //  ZCOUNT key min max
    //  ZINCRBY key increment member
    //  ZINTER numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX] [WITHSCORES]
    //  ZINTERSTORE destination numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX]
    //  ZLEXCOUNT key min max
    //  ZMSCORE key member [member ...]
    //  ZPOPMAX key [count]
    //  ZPOPMIN key [count]
    //  ZRANGE key start stop [WITHSCORES]
    //  ZRANGEBYLEX key min max [LIMIT offset count]
    //  ZRANGEBYSCORE key min max [WITHSCORES] [LIMIT offset count]
    //  ZRANK key member
    //  ZREM key member [member ...]
    //  ZREMRANGEBYLEX key min max
    //  ZREMRANGEBYRANK key start stop
    //  ZREMRANGEBYSCORE key min max
    //  ZREVRANGE key start stop [WITHSCORES]
    //  ZREVRANGEBYLEX key max min [LIMIT offset count]
    //  ZREVRANGEBYSCORE key max min [WITHSCORES] [LIMIT offset count]
    //  ZREVRANK key member
    //  ZSCAN key cursor [MATCH pattern] [COUNT count]
    //  ZSCORE key member
    //  ZUNION numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX] [WITHSCORES]
    //  ZUNIONSTORE destination numkeys key [key ...] [WEIGHTS weight [weight ...]] [AGGREGATE SUM|MIN|MAX]
}
