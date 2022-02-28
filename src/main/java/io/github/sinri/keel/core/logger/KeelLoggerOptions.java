package io.github.sinri.keel.core.logger;

import io.github.sinri.keel.core.properties.KeelConfigurationBasement;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.nio.charset.Charset;

public class KeelLoggerOptions extends KeelConfigurationBasement {

    public KeelLoggerOptions() {
        super(new JsonObject()
                .put("aspect", "default")
                .put("dir", null)
                .put("lowestLevel", "INFO")
                .put("rotateDateTimeFormat", "yyyyMMdd")
                .put("keepWriterReady", "YES")
                .put("showThreadID", "YES")
                .put("fileOutputCharset", null)
        );
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

    public File getDir() {
        String x = this.getJsonObject().getString("dir");
        if (x == null) return null;
        return new File(x);
    }

    public KeelLoggerOptions setDir(File dir) {
        this.getJsonObject().put("dir", dir);
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
