package io.github.sinri.keel.maids.gatling;

import io.github.sinri.keel.core.logger.KeelLogger;
import io.vertx.core.Future;

import java.util.function.Supplier;

public class KeelGatlingOptions {
    private final String gatlingName;
    private int barrels;
    private int averageRestInterval;
    private Supplier<Future<Bullet>> bulletLoader;
    private KeelLogger logger;

    public KeelGatlingOptions(String gatlingName) {
        this.gatlingName = gatlingName;
        this.barrels = 1;
        this.averageRestInterval = 1000;
        this.bulletLoader = () -> Future.succeededFuture(null);
        this.logger = KeelLogger.silentLogger();
    }

    /**
     * @return 加特林机枪名称（集群中各节点之间的识别同一组加特林机枪类的实例用）
     */
    public String getGatlingName() {
        return gatlingName;
    }

    /**
     * @return 枪管数量（并发任务数）
     */
    public int getBarrels() {
        return barrels;
    }

    /**
     * @param barrels 枪管数量（并发任务数）
     */
    public KeelGatlingOptions setBarrels(int barrels) {
        this.barrels = barrels;
        return this;
    }

    /**
     * @return 弹带更换平均等待时长（没有新任务时的休眠期，单位0.001秒）
     */
    public int getAverageRestInterval() {
        return averageRestInterval;
    }

    /**
     * @param averageRestInterval 弹带更换平均等待时长（没有新任务时的休眠期，单位0.001秒）
     */
    public KeelGatlingOptions setAverageRestInterval(int averageRestInterval) {
        this.averageRestInterval = averageRestInterval;
        return this;
    }

    /**
     * @return 供弹器（新任务生成器）
     */
    public Supplier<Future<Bullet>> getBulletLoader() {
        return bulletLoader;
    }

    /**
     * @param bulletLoader 供弹器（新任务生成器）
     */
    public KeelGatlingOptions setBulletLoader(Supplier<Future<Bullet>> bulletLoader) {
        this.bulletLoader = bulletLoader;
        return this;
    }

    /**
     * @return 日志记录仪
     */
    public KeelLogger getLogger() {
        return logger;
    }

    /**
     * @param logger 日志记录仪
     */
    public KeelGatlingOptions setLogger(KeelLogger logger) {
        this.logger = logger;
        return this;
    }


}
