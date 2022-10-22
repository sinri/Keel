package io.github.sinri.keel.servant.funnel;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.verticles.KeelVerticleInterface;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;

import java.util.function.Supplier;

/**
 * 随时接收小任务，并周期性轮询依次执行。
 * 一般在构建新的实例（new）或部署为verticle（deploy）时自动开始。
 * KeelSisiodosi receives tasks (as drips) continually,
 * and holds them for a batch handle job.
 *
 * @since 2.9 rename from sisiodosi to funnel
 */
public interface KeelFunnel extends KeelVerticleInterface {
    static KeelFunnel getOneInstanceToDeploy(KeelFunnelImpl.Options options) {
        return new KeelFunnelImpl()
                .setOptions(options);
    }

    static Future<KeelFunnel> deployOneInstance(KeelFunnelImpl.Options options) {
        KeelFunnelImpl keelSisiodosiWithTimer = new KeelFunnelImpl()
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
     * @return 一回にともに処理する雫の数
     */
    default int getBatchDrips() {
        return 1;
    }

    /**
     * @param drip 雫。それを処理して、一つの予期を返してすら、次の雫に移行するか終わるかが許される。
     */
    void drop(Supplier<Future<Object>> drip);
}
