package io.github.sinri.keel.lagecy.core.logger;

import io.github.sinri.keel.core.properties.KeelOptions;
import io.github.sinri.keel.lagecy.Keel;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

public class KeelLoggerOptions {
    private final KeelLoggerOptionsPOJO pojo;

    // private zone
    private final Set<String> ignorableStackPackageSet = new HashSet<>();
    private final List<String> aspectComponentList = new ArrayList<>();
    private String aspect;

    public KeelLoggerOptions() {
        this.pojo = new KeelLoggerOptionsPOJO();
        this.setAspect("default");
    }

    /**
     * @param aspect Aspect of logger
     * @return KeelLoggerOptions
     * @since 2.2
     */
    public KeelLoggerOptions loadForAspect(String aspect) {
        JsonObject x = Keel.getPropertiesReader().filter("log").toJsonObject();
        if (x.containsKey("*")) {
            this.pojo.overwritePropertiesWithJsonObject(x.getJsonObject("*"));
        }
        String[] aspectComponents = aspect.split("/");
        StringBuilder aspectPart = new StringBuilder();
        if (aspectComponents.length > 1) {
            for (var aspectComponent : aspectComponents) {
                aspectPart.append((aspectPart.length() == 0) ? "" : "/").append(aspectComponent);
                if (x.containsKey(aspectPart.toString())) {
                    this.pojo.overwritePropertiesWithJsonObject(x.getJsonObject(aspectPart.toString()));
                }
            }
        }
        if (x.containsKey(aspect)) {
            this.pojo.overwritePropertiesWithJsonObject(x.getJsonObject(aspect));
        }

        // refresh
        this.setAspect(aspect);
        this.ignorableStackPackageSet.addAll(Arrays.asList(pojo.ignorableStackPackages.split("[\\s,;]+")));

        return this;
    }

    public String getAspect() {
        return aspect;
    }

    public void setAspect(String aspect) {
        this.aspect = aspect;
        String[] aspectParts = this.aspect.split("[/\\\\]+");
        aspectComponentList.clear();
        for (var x : aspectParts) {
            if (x == null || x.trim().equalsIgnoreCase("")) continue;
            aspectComponentList.add(x);
        }
        if (aspectComponentList.isEmpty()) {
            aspectComponentList.add("default");
        }
    }

    public List<String> getAspectComponentList() {
        return aspectComponentList;
    }

    public String getSubject() {
        return aspectComponentList.get(aspectComponentList.size() - 1);
    }

    public KeelLogLevel getLowestVisibleLogLevel() {
        return KeelLogLevel.valueOf(this.pojo.level);
    }

    public KeelLoggerOptions setLowestVisibleLogLevel(KeelLogLevel logLevel) {
        this.pojo.level = logLevel.name();
        return this;
    }

    public KeelLoggerOptions addIgnorableStackPackage(String packageName) {
        if (this.pojo.ignorableStackPackages == null) {
            this.pojo.ignorableStackPackages = packageName;
        } else {
            this.pojo.ignorableStackPackages += "," + packageName;
        }
        return this;
    }

    public String matchIgnorableStackPackage(String classOfStackItem) {
        for (var ignorableStackPackage : ignorableStackPackageSet) {
            if (classOfStackItem.startsWith(ignorableStackPackage)) {
                return ignorableStackPackage;
            }
        }
        return null;
    }

    /**
     * @since 2.9
     */
    public Set<String> getIgnorableStackPackageSet() {
        return ignorableStackPackageSet;
    }

    public CompositionStyle getCompositionStyle() {
        return Objects.requireNonNullElse(CompositionStyle.valueOf(this.pojo.compositionStyle), CompositionStyle.ONE_LINE);
    }

    public KeelLoggerOptions setCompositionStyle(CompositionStyle compositionStyle) {
        pojo.compositionStyle = compositionStyle.name();
        return this;
    }

    /**
     * @return 日志当写到何处；如果为配置中未指明具体路径，则返回 null。
     */
    public File getDir() {
        if (pojo.dir == null || pojo.dir.isEmpty()) {
            return null;
        }
        return new File(pojo.dir);
    }

    public KeelLoggerOptions setDir(String dirPath) {
        pojo.dir = dirPath;
        return this;
    }

    public Charset getFileOutputCharset() {
        String x = this.pojo.fileOutputCharset;
        if (x == null || x.isEmpty()) {
            return Charset.defaultCharset();
        }
        return Charset.forName(x);
    }

    public KeelLoggerOptions setFileOutputCharset(Charset charset) {
        pojo.fileOutputCharset = charset.name();
        return this;
    }

    public boolean shouldShowThreadID() {
        return pojo.showThreadID;
    }

    public KeelLoggerOptions setShowThreadID(boolean showThreadID) {
        pojo.showThreadID = showThreadID;
        return this;
    }

    public boolean shouldShowVerticleDeploymentID() {
        return pojo.showVerticleDeploymentID;
    }

    public KeelLoggerOptions setShowVerticleDeploymentID(boolean showVerticleDeploymentID) {
        pojo.showVerticleDeploymentID = showVerticleDeploymentID;
        return this;
    }

    public String getFileRotateFormat() {
        return pojo.rotate;
    }

    public KeelLoggerOptions setFileRotateFormat(String fileRotateFormat) {
        this.pojo.rotate = fileRotateFormat;
        return this;
    }

    public String getArchivePath() {
        return this.pojo.archivePath;
    }

    public KeelLoggerOptions setArchivePath(String archivePath) {
        this.pojo.archivePath = archivePath;
        return this;
    }

    public String getImplement() {
        return pojo.implement;
    }

    public KeelLoggerOptions setImplement(String implement) {
        this.pojo.implement = implement;
        return this;
    }

    public boolean shouldKeepWriterReady() {
        return pojo.keepWriterReady;
    }

    public KeelLoggerOptions setKeepWriterReady(boolean keepWriterReady) {
        this.pojo.keepWriterReady = keepWriterReady;
        return this;
    }

    public enum CompositionStyle {
        ONE_LINE, // meta + message + context \n
        TWO_LINES,// meta \n message + context \n
        THREE_LINES,// meta \n message \n context \n
        ONE_JSON_OBJECT,// {K:V,...}
    }

    protected static class KeelLoggerOptionsPOJO extends KeelOptions {
        // pojo zone

        /**
         * 在 KeelLogger 的 createLogger 方法中用于指明使用哪一个实现类进行实例化.
         * 预定义的值有 sync, silent, print, async;
         * 此外可以使用 KeelLogger 实现类的全名。
         *
         * @see KeelLogger#createLogger(KeelLoggerOptions)
         */
        public String implement;
        /**
         * 指明日志当写到何处。
         * 一般应指定为某个文件夹的路径；
         * 也可以为一个已存在的文件的路径（如果文件不存在，文件名将被视为待新建的目录名）。
         *
         * @see KeelLoggerOptions#getDir()
         */
        public String dir;
        /**
         * 日志可输出的最低级别
         *
         * @see KeelLoggerOptions#getLowestVisibleLogLevel()
         */
        public String level;
        /**
         * 决定日志输出文件是否按时间滚动分割；
         * 如果其非空，则文件名中加上按此时间格式所示标签。
         */
        public String rotate;
        /**
         * 决定日志输出文件归档的相应目录；
         * 如果其非空，则目录按此细分归档。
         * FORMAT SAMPLE: A/B/C-{yyyy-MM-dd...}/D
         */
        public String archivePath;
        /**
         * 如果日志输出目标需要且可以手动开启关闭，此选项可以用于决定是否保持输出器常开。
         */
        public boolean keepWriterReady;
        /**
         * 决定是否在每一条日志格式中定义当前线程的ID。
         *
         * @see AbstractKeelLogger#createTextFromLog(KeelLogLevel, String, JsonObject)
         */
        public boolean showThreadID;
        /**
         * 决定是否在每一条日志格式中定义当前Verticle的DeploymentID。
         *
         * @see AbstractKeelLogger#createTextFromLog(KeelLogLevel, String, JsonObject)
         */
        public boolean showVerticleDeploymentID;
        /**
         * 用于指示日志输出是否需要使用指定的字符集。
         * 不指定则使用系统默认的字符集。
         *
         * @see KeelLoggerOptions#getFileOutputCharset()
         */
        public String fileOutputCharset;
        /**
         * 按照 CompositionStyle 的定义，设置每条日志的显示格式（单行，双行，三行）。
         *
         * @see CompositionStyle
         * @see KeelLoggerOptions#getCompositionStyle()
         */
        public String compositionStyle;
        /**
         * 用于生成 ignorableStackPackageSet 的内容并用于判断调用栈中可忽略的类记录。
         *
         * @see KeelLoggerOptions#matchIgnorableStackPackage(String)
         */
        public String ignorableStackPackages;
        public KeelLoggerOptionsPOJO() {
            this.dir = null;
            this.level = KeelLogLevel.INFO.name();
            this.rotate = "yyyyMMdd";
            this.keepWriterReady = true;
            this.showThreadID = true;
            this.fileOutputCharset = null;
            this.archivePath = null;
            this.showVerticleDeploymentID = true;
            this.compositionStyle = CompositionStyle.ONE_LINE.name();
            this.implement = null;
            this.ignorableStackPackages = null;
        }
    }
}
