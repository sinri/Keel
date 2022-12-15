package io.github.sinri.keel.lagecy.core.logger.async;

import io.github.sinri.keel.lagecy.core.logger.KeelLogLevel;

import java.util.HashSet;
import java.util.Set;

/**
 * @since 2.9
 */
public class KeelAsyncQueueLoggerOptions {
    /**
     * @since 2.9.3
     */
    private String implement;
    private KeelLogLevel lowestVisibleLogLevel;
    private Set<String> ignorableStackPackageSet;
    private String aspect;
    /**
     * Aliyun SLS 中的概念，比 aspect 更高级的分组概念。
     *
     * @since 2.9.3
     */
    private String topic;

    private boolean useTee;
    private boolean showThreadID;
    private boolean showVerticleDeploymentID;

    public KeelAsyncQueueLoggerOptions() {
        this.implement = "";
        this.topic = "";

        this.lowestVisibleLogLevel = KeelLogLevel.INFO;
        this.aspect = "default";
        this.useTee = false;
        this.showThreadID = true;
        this.showVerticleDeploymentID = true;

        this.ignorableStackPackageSet = new HashSet<>();
        this.ignorableStackPackageSet.add("io.vertx");
        this.ignorableStackPackageSet.add("io.netty");
        this.ignorableStackPackageSet.add("java.lang");
    }

    /**
     * @return "" or SPI class name
     * @since 2.9.3
     */
    public String getImplement() {
        return implement;
    }

    /**
     * @param implement "" or SPI class name
     * @since 2.9.3
     */
    public KeelAsyncQueueLoggerOptions setImplement(String implement) {
        this.implement = implement;
        return this;
    }

    /**
     * @since 2.9.3
     */
    public String getTopic() {
        return topic;
    }

    /**
     * @since 2.9.3
     */
    public KeelAsyncQueueLoggerOptions setTopic(String topic) {
        this.topic = topic;
        return this;
    }

    public String getAspect() {
        return aspect;
    }

    public KeelAsyncQueueLoggerOptions setAspect(String aspect) {
        this.aspect = aspect;
        return this;
    }

    public KeelLogLevel getLowestVisibleLogLevel() {
        return lowestVisibleLogLevel;
    }

    public KeelAsyncQueueLoggerOptions setLowestVisibleLogLevel(KeelLogLevel lowestVisibleLogLevel) {
        this.lowestVisibleLogLevel = lowestVisibleLogLevel;
        return this;
    }

    public Set<String> getIgnorableStackPackageSet() {
        return ignorableStackPackageSet;
    }

    public KeelAsyncQueueLoggerOptions setIgnorableStackPackageSet(Set<String> ignorableStackPackageSet) {
        this.ignorableStackPackageSet = ignorableStackPackageSet;
        return this;
    }

    public boolean isUseTee() {
        return useTee;
    }

    public KeelAsyncQueueLoggerOptions setUseTee(boolean useTee) {
        this.useTee = useTee;
        return this;
    }

    public boolean shouldShowThreadID() {
        return showThreadID;
    }

    public KeelAsyncQueueLoggerOptions setShowThreadID(boolean showThreadID) {
        this.showThreadID = showThreadID;
        return this;
    }

    public boolean shouldShowVerticleDeploymentID() {
        return showVerticleDeploymentID;
    }

    public KeelAsyncQueueLoggerOptions setShowVerticleDeploymentID(boolean showVerticleDeploymentID) {
        this.showVerticleDeploymentID = showVerticleDeploymentID;
        return this;
    }
}
