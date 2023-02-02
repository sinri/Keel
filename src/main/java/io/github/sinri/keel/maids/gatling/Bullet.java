package io.github.sinri.keel.maids.gatling;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

import java.util.Set;

/**
 * @since 2.9.1
 */
abstract public class Bullet {
    abstract public String bulletID();

    abstract protected Set<String> exclusiveLockSet();

    abstract protected Future<Object> fire();

    abstract protected Future<Void> ejectShell(AsyncResult<Object> fired);
}
