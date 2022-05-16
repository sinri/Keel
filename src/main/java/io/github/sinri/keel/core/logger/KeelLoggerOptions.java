package io.github.sinri.keel.core.logger;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.properties.KeelOptions;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.nio.charset.Charset;

/**
 * 这是一个遵循 KeelOptions 定义的 POJO 类，用于 KeelLogger 的初始化。
 * 此文件可以从 properties 配置文件中获取。
 * 本类提供了静态方法以从 Keel 的标准配置文件中查找给定的 aspect 对应的配置并根据结果来生成实例。
 * 注意，aspect 不是 property，而是POJO中所有 properties 的归类名。
 */
public class KeelLoggerOptions extends KeelOptions {
    public String dir;
    public String level;//lowestLevel
    public String rotate;//rotateDateTimeFormat
    public String archive;
    public boolean keepWriterReady;
    public boolean showThreadID;
    public String fileOutputCharset;
    protected String aspect;

    public KeelLoggerOptions() {
        this.aspect = "default";
        this.dir = null;
        this.level = "INFO";
        this.rotate = "yyyyMMdd";
        this.keepWriterReady = true;
        this.showThreadID = true;
        this.fileOutputCharset = null;
        this.archive = null;
    }

    /**
     * @param aspect the string of aspect
     * @return KeelLoggerOptions, read and composed of the content read by reader
     */
    public static KeelLoggerOptions generateOptionsForAspectWithPropertiesReader(String aspect) {
        JsonObject x = Keel.getPropertiesReader().filter("log").toJsonObject();
        KeelLoggerOptions keelLoggerOptions = new KeelLoggerOptions();
        if (x.containsKey("*")) {
            keelLoggerOptions.overwritePropertiesWithJsonObject(x.getJsonObject("*"));
        }
        String[] aspectComponents = aspect.split("/");
        StringBuilder aspectPart = new StringBuilder();
        if (aspectComponents.length > 1) {
            for (var aspectComponent : aspectComponents) {
                aspectPart.append((aspectPart.length() == 0) ? "" : "/").append(aspectComponent);
                if (x.containsKey(aspectPart.toString())) {
                    keelLoggerOptions.overwritePropertiesWithJsonObject(x.getJsonObject(aspectPart.toString()));
                }
            }
        }
        if (x.containsKey(aspect)) {
            keelLoggerOptions.overwritePropertiesWithJsonObject(x.getJsonObject(aspect));
        }
        keelLoggerOptions.setAspect(aspect);
        return keelLoggerOptions;
    }

    public String getAspect() {
        return aspect;
    }

    public KeelLoggerOptions setAspect(String aspect) {
        this.aspect = aspect;
        return this;
    }

    public File getDir() {
        if (this.dir == null) return null;
        return new File(this.dir);
    }

    public KeelLoggerOptions setDir(String dir) {
        if (dir == null || dir.isEmpty()) {
            this.dir = null;
        } else {
            this.dir = dir;
        }
        return this;
    }

    public KeelLoggerOptions setDir(File dir) {
        if (dir == null) {
            this.dir = null;
        } else {
            this.dir = dir.getAbsolutePath();
        }
        return this;
    }

    public KeelLogLevel getLowestLevel() {
        return KeelLogLevel.valueOf(this.level);
    }

    public KeelLoggerOptions setLowestLevel(KeelLogLevel lowestLevel) {
        this.level = lowestLevel.name();
        return this;
    }

    public KeelLoggerOptions setRotateDateTimeFormat(String rotateDateTimeFormat) {
        this.rotate = rotateDateTimeFormat;
        return this;
    }

    public boolean isKeepWriterReady() {
        return this.keepWriterReady;
    }

    public KeelLoggerOptions setKeepWriterReady(boolean keepWriterReady) {
        this.keepWriterReady = keepWriterReady;
        return this;
    }

    public boolean isShowThreadID() {
        return this.showThreadID;
    }

    public KeelLoggerOptions setShowThreadID(boolean showThreadID) {
        this.showThreadID = showThreadID;
        return this;
    }

    public Charset getFileOutputCharset() {
        String x = this.fileOutputCharset;
        if (x == null) {
            return Charset.defaultCharset();
        }
        return Charset.forName(x);
    }

    public KeelLoggerOptions setFileOutputCharset(Charset fileOutputCharset) {
        this.fileOutputCharset = fileOutputCharset.name();
        return this;
    }
}
