package io.github.sinri.keel.test.hazelcast.gatling;

import io.github.sinri.keel.lagecy.Keel;
import io.github.sinri.keel.maids.gatling.Bullet;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class TestBullet extends Bullet {
    private final String bulletID;
    private final Set<String> exclusiveLockSet;
    private final int param;
    private final Random random = new Random();

    public TestBullet(int i) {
        this.bulletID = "Bullet-" + i;
        this.param = i;
        this.exclusiveLockSet = new HashSet<>();

        if (param % 2 == 0) {
            this.exclusiveLockSet.add("two");
        }
        if (param % 3 == 0) {
            this.exclusiveLockSet.add("three");
        }
    }

    @Override
    public String bulletID() {
        return bulletID;
    }

    @Override
    protected Set<String> exclusiveLockSet() {
        return exclusiveLockSet;
    }

    @Override
    protected Future<Object> fire() {
        long i = random.nextInt(10_000);
        Keel.outputLogger("Bullet").info("FIRING " + this.param);
        return Keel.callFutureSleep(i)
                .map(slept -> {
                    Keel.outputLogger("Bullet").info("FIRED " + this.param);
                    return Future.succeededFuture();
                });
    }

    @Override
    protected Future<Void> ejectShell(AsyncResult<Object> fired) {
        return Future.succeededFuture();
    }
}
