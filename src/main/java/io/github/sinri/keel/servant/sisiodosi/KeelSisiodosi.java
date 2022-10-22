package io.github.sinri.keel.servant.sisiodosi;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.verticles.KeelVerticleInterface;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.function.Supplier;

/**
 * ししおどし（鹿威し）とは、田畑を荒らす鳥獣を威嚇し追い払うために設けられる装置類の総称。
 * 一般在构建新的实例（new）或部署为verticle（deploy）时自动开始。
 * KeelSisiodosi receives tasks (as drips) continually,
 * and holds them for a batch handle job.
 *
 * @since 2.8.1
 */
public interface KeelSisiodosi extends KeelVerticleInterface {
    static void deployOneInstance(
            KeelSisiodosiWithTimer.Options options,
            Handler<KeelSisiodosi> keelSisiodosiHandler
    ) {
        KeelSisiodosiWithTimer keelSisiodosiWithTimer = new KeelSisiodosiWithTimer()
                .setOptions(options);
        Keel.getVertx().deployVerticle(
                keelSisiodosiWithTimer,
                new DeploymentOptions().setWorker(true),
                ar -> {
                    if (ar.succeeded()) {
                        keelSisiodosiHandler.handle(keelSisiodosiWithTimer);
                    } else {
                        throw new RuntimeException(new RuntimeException(ar.cause()));
                    }
                }
        );
    }

    static Future<KeelSisiodosi> deployOneInstance(KeelSisiodosiWithTimer.Options options) {
        KeelSisiodosiWithTimer keelSisiodosiWithTimer = new KeelSisiodosiWithTimer()
                .setOptions(options);
        return Keel.getVertx().deployVerticle(
                        keelSisiodosiWithTimer,
                        new DeploymentOptions().setWorker(true)
                )
                .compose(d -> {
                    return Future.succeededFuture(keelSisiodosiWithTimer);
                });
    }

    /**
     * @return 鹿威しの中の雫の数
     */
    int getTotalDrips();

    /**
     * @return 暇のうちに、その時間を経て、鹿威しの中身を検査し、条件満たされたら雫を処理し始まる、そうでないと続きの暇。
     */
    long getQueryInterval();

    /**
     * @return 鹿威しの中の雫があるが最低限の数に足りない時、すでにどれほどの暇が過ぎたら雫を処理し始まる。
     */
    long getTimeThreshold();

    /**
     * @return 鹿威しの中の雫が処理を始まるが必要の最低限の数。
     */
    int getSizeThreshold();

    /**
     * @param drip 雫。それを処理して、一つの予期を返してすら、次の雫に移行するか終わるかが許される。
     */
    void drop(Supplier<Future<Object>> drip);
}
