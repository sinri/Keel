package io.github.sinri.keel.core.logger;

import io.github.sinri.keel.core.KeelHelper;
import io.vertx.core.json.JsonObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KeelLoggerDelegate {
    protected KeelLoggerOptions options;
    protected String categoryPrefix = null;
    protected List<String> aspectComponentList = new ArrayList<>();
    protected File readyFile = null;
    protected BufferedWriter readyWriter = null;

    public KeelLoggerDelegate() {
        this.options = new KeelLoggerOptions();
        this.parseAspect();
    }

    public KeelLoggerDelegate(KeelLoggerOptions options) {
        this.options = options;
        this.parseAspect();
    }

    /**
     * @param format "yyyyMMdd" or "yyyy-MM-dd HH:mm:ss", etc. if null, return null
     * @return the date string or null
     */
    public static String getCurrentDateExpression(String format) {
        if (format == null || format.isEmpty()) {
            return null;
        }
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(currentTime);
    }

    protected void parseAspect() {
        String[] aspectParts = this.options.aspect.split("[/\\\\]+");
        aspectComponentList.clear();
        for (var x : aspectParts) {
            if (x == null || x.trim().equalsIgnoreCase("")) continue;
            aspectComponentList.add(x);
        }
        if (aspectComponentList.isEmpty()) {
            aspectComponentList.add("default");
        }
    }

    /**
     * @param categoryPrefix category prefix
     * @return this
     * @since 1.11
     */
    public KeelLoggerDelegate setCategoryPrefix(String categoryPrefix) {
        this.categoryPrefix = categoryPrefix;
        if (this.options.isKeepWriterReady()) {
            resetReadyWriter();
        }
        return this;
    }

    protected void resetReadyWriter() {
        try {
            if (readyWriter != null) {
                readyWriter.close();
            }
        } catch (IOException e) {
            new KeelLogger().warning("Failed to close readyWriter: " + e.getMessage());
        } finally {
            readyWriter = null;
            readyFile = null;
        }
    }

    /**
     * From Aspect and Category Prefix
     * Aspect: a | a/b
     * Category Prefix: EMPTY | x
     *
     * @return computed Relative Directory Path
     */
    protected String computeRelativeDirPath() {
        return KeelHelper.joinStringArray(aspectComponentList, File.separator);
    }

    protected String computeFileName() {
        String rotateDateTimeFormat = this.options.rotate;
        String prefix;
        if (this.categoryPrefix != null && !this.categoryPrefix.isEmpty()) {
            prefix = this.categoryPrefix;
        } else {
            prefix = aspectComponentList.get(aspectComponentList.size() - 1);
        }
        String currentDateExpression = getCurrentDateExpression(rotateDateTimeFormat);
        if (currentDateExpression != null) {
            return prefix + "-" + currentDateExpression + ".log";
        } else {
            return prefix + ".log";
        }
    }

    protected File getOutputTargetFile() {
        File logRootDirectory = this.options.getDir();
        if (logRootDirectory == null) {
            // directly output to stdout, FORMAT
            // DATETIME [LEVEL] <ASPECT> MSG | CONTEXT
            return null;
        }

        if (logRootDirectory.exists()) {
            if (logRootDirectory.isFile()) {
                // directly output to the file `logRootDirectory`, FORMAT
                // DATETIME [LEVEL] <ASPECT> MSG | CONTEXT
                return logRootDirectory;
            }
        }

        File dir = new File(logRootDirectory.getAbsolutePath() + File.separator + computeRelativeDirPath());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                new KeelLogger().warning("Cannot MKDIRS: " + dir.getAbsolutePath());
                //throw new IOException("Cannot MKDIRS: " + dir.getAbsolutePath());
            }
        }

        String realPath = dir.getAbsolutePath() + File.separator + computeFileName();

        return new File(realPath);
    }

    protected BufferedWriter getWriter(File outputTargetFile) throws IOException {
        if (this.options.isKeepWriterReady()) {
            if (readyWriter != null) {
                if (readyFile == null || !outputTargetFile.getAbsolutePath().equals(readyFile.getAbsolutePath())) {
                    resetReadyWriter();
                }
            }
            if (readyWriter == null) {
                readyWriter = new BufferedWriter(new FileWriter(outputTargetFile, this.options.getFileOutputCharset(), true));
                readyFile = outputTargetFile;
            }
            return readyWriter;
        } else {
            return new BufferedWriter(new FileWriter(outputTargetFile, this.options.getFileOutputCharset(), true));
        }
    }

    public void log(KeelLogLevel level, String msg, JsonObject context) {
        if (level.isSilent() || level.isNegligibleThan(this.options.getLowestLevel())) {
            return;
        }
        String subject = aspectComponentList.get(aspectComponentList.size() - 1);
        if (categoryPrefix != null && !categoryPrefix.isEmpty()) {
            subject += ":" + categoryPrefix;
        }
        String content = getCurrentDateExpression("yyyy-MM-dd HH:mm:ss") + " "
                + "[" + level.name() + "] "
                + "<" + subject + "> "
                + (this.options.isShowThreadID() ? ("[" + Thread.currentThread().getId() + "] ") : "")
                + msg;
        if (context != null) {
            content += " | " + context;
        }
        print(content, null);
    }

    /**
     * It would always print the content to STDOUT, but any occurred errors would be printed to STDERR.
     *
     * @param content the String content
     * @param ending  Ending String as line separator, or null to use default
     */
    private void print(String content, String ending) {
        File outputTargetFile = getOutputTargetFile();
        if (outputTargetFile == null) {
            if (ending == null) {
                System.out.println(content);
            } else {
                System.out.print(content);
                System.out.print(ending);
            }
        } else {
            try {
                BufferedWriter writer = getWriter(outputTargetFile);
                writer.write(content);
                if (ending == null) {
                    writer.newLine();
                } else {
                    writer.write(ending);
                }
                writer.flush();
                if (!this.options.isKeepWriterReady()) {
                    writer.close();
                }
            } catch (IOException e) {
                new KeelLogger().exception(e);
                System.err.println(content);
            }
        }
    }

    /**
     * @param level   KeelLogLevel
     * @param content content
     * @param ending  Ending String as line separator, or null to use default
     * @since 1.11
     */
    public void print(KeelLogLevel level, String content, String ending) {
        if (level.isSilent() || level.isNegligibleThan(this.options.getLowestLevel())) {
            return;
        }
        print(content, ending);
    }
}
