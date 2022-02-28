package io.github.sinri.keel.core.logger;

import io.github.sinri.keel.core.properties.KeelConfigurationBasement;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.nio.charset.Charset;

public class KeelLoggerOptions extends KeelConfigurationBasement {
//    protected String aspect;
//    protected File logRootDirectory;
//    protected KeelLogLevel lowestLevel;
//    protected String rotateDateTimeFormat;
//    protected boolean keepWriterReady;
//    protected boolean showThreadID;
//    protected Charset fileOutputCharset;

    public KeelLoggerOptions() {
        super(new JsonObject()
                .put("aspect", "default")
                .put("logRootDirectory", null)
                .put("lowestLevel", "INFO")
                .put("rotateDateTimeFormat", "yyyyMMdd")
                .put("keepWriterReady", "YES")
                .put("showThreadID", "YES")
                .put("fileOutputCharset", null)
        );

//        this.aspect = "default";
//        this.logRootDirectory = null;
//        this.lowestLevel = KeelLogLevel.INFO;
//        this.rotateDateTimeFormat = "yyyyMMdd";
//        this.keepWriterReady = true;
//        this.showThreadID = true;
//        this.fileOutputCharset = Charset.defaultCharset();
    }

    public KeelLoggerOptions(JsonObject jsonObject) {
        super(jsonObject);
    }

    public String getAspect() {
        return this.getJsonObject().getString("aspect");
    }

    public KeelLoggerOptions setAspect(String aspect) {
        this.getJsonObject().put("aspect", aspect);
        return this;
    }

    public File getLogRootDirectory() {
        String x = this.getJsonObject().getString("logRootDirectory");
        if (x == null) return null;
        return new File(x);
    }

    public KeelLoggerOptions setLogRootDirectory(File logRootDirectory) {
        this.getJsonObject().put("logRootDirectory", logRootDirectory);
        return this;
    }

    public KeelLogLevel getLowestLevel() {
        String lowestLevel = this.getJsonObject().getString("lowestLevel");
        return KeelLogLevel.valueOf(lowestLevel);
    }

    public KeelLoggerOptions setLowestLevel(KeelLogLevel lowestLevel) {
        this.getJsonObject().put("lowestLevel", lowestLevel.name());
        return this;
    }

    public String getRotateDateTimeFormat() {
        return this.getJsonObject().getString("rotateDateTimeFormat");
    }

    public KeelLoggerOptions setRotateDateTimeFormat(String rotateDateTimeFormat) {
        this.getJsonObject().put("rotateDateTimeFormat", rotateDateTimeFormat);
        return this;
    }

    public boolean isKeepWriterReady() {
        return "YES".equalsIgnoreCase(this.getJsonObject().getString("keepWriterReady"));
    }

    public KeelLoggerOptions setKeepWriterReady(boolean keepWriterReady) {
        this.getJsonObject().put("keepWriterReady", keepWriterReady ? "YES" : "NO");
        return this;
    }

    public boolean isShowThreadID() {
        return "YES".equalsIgnoreCase(this.getJsonObject().getString("showThreadID"));
    }

    public KeelLoggerOptions setShowThreadID(boolean showThreadID) {
        this.getJsonObject().put("showThreadID", showThreadID ? "YES" : "NO");
        return this;
    }

    public Charset getFileOutputCharset() {
        String x = this.getJsonObject().getString("fileOutputCharset");
        if (x == null) {
            return Charset.defaultCharset();
        }
        return Charset.forName(x);
    }

    public KeelLoggerOptions setFileOutputCharset(Charset fileOutputCharset) {
        this.getJsonObject().put("fileOutputCharset", fileOutputCharset.name());
        return this;
    }
}
