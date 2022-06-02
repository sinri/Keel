package io.github.sinri.keel.core.logger;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.KeelHelper;
import io.github.sinri.keel.core.properties.KeelOptions;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    public boolean showVerticleDeploymentID;
    public String fileOutputCharset;
    public String compositionStyle;
    public String ignorableStackPackages;

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
        this.showVerticleDeploymentID = true;
        this.compositionStyle = CompositionStyle.ONE_LINE.name();
    }

    /**
     * @param aspect Aspect of logger
     * @return KeelLoggerOptions
     * @since 2.2
     */
    public KeelLoggerOptions loadForAspect(String aspect) {
        JsonObject x = Keel.getPropertiesReader().filter("log").toJsonObject();
        if (x.containsKey("*")) {
            this.overwritePropertiesWithJsonObject(x.getJsonObject("*"));
        }
        String[] aspectComponents = aspect.split("/");
        StringBuilder aspectPart = new StringBuilder();
        if (aspectComponents.length > 1) {
            for (var aspectComponent : aspectComponents) {
                aspectPart.append((aspectPart.length() == 0) ? "" : "/").append(aspectComponent);
                if (x.containsKey(aspectPart.toString())) {
                    this.overwritePropertiesWithJsonObject(x.getJsonObject(aspectPart.toString()));
                }
            }
        }
        if (x.containsKey(aspect)) {
            this.overwritePropertiesWithJsonObject(x.getJsonObject(aspect));
        }
        this.setAspect(aspect);
        return this;
    }

    public String getAspect() {
        return aspect;
    }

    protected KeelLoggerOptions setAspect(String aspect) {
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

    /**
     * @return is Show Verticle Deployment ID
     * @since 2.2
     */
    public boolean isShowVerticleDeploymentID() {
        return showVerticleDeploymentID;
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

    /**
     * @return CompositionStyle
     * @since 2.2
     */
    public CompositionStyle getCompositionStyle() {
        return CompositionStyle.valueOf(compositionStyle);
    }

    /**
     * @param compositionStyle CompositionStyle
     * @return KeelLoggerOptions
     * @since 2.2
     */
    public KeelLoggerOptions setCompositionStyle(CompositionStyle compositionStyle) {
        this.compositionStyle = compositionStyle.name();
        return this;
    }

    /**
     * @since 2.5
     */
    public List<String> getIgnorableStackPackages() {
        List<String> list = new ArrayList<>();
        if (this.ignorableStackPackages != null) {
            String[] split = this.ignorableStackPackages.split("[\\s;,]+");
            for (var x : split) {
                var y = x.trim();
                if (!y.isEmpty()) {
                    list.add(y);
                }
            }
        }
        return list;
    }

    /**
     * @since 2.5
     */
    public KeelLoggerOptions setIgnorableStackPackages(String ignorableStackPackages) {
        this.ignorableStackPackages = ignorableStackPackages;
        return this;
    }

    /**
     * @since 2.5
     */
    public KeelLoggerOptions setThrowableStackIgnorePackages(Collection<String> throwableStackIgnorePackages) {
        this.ignorableStackPackages = KeelHelper.joinStringArray(throwableStackIgnorePackages, ",");
        return this;
    }

    /**
     * @since 2.5
     */
    public String verifyIgnorableThrowableStackPackage(String stack) {
        for (var x : this.getIgnorableStackPackages()) {
            if (stack.startsWith(x)) {
                return x;
            }
        }
        return null;
    }

    /**
     * @since 2.2
     */
    public enum CompositionStyle {
        ONE_LINE, // meta + message + context \n
        TWO_LINES,// meta \n message + context \n
        THREE_LINES,// meta \n message \n context \n
    }
}
