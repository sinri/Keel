package io.github.sinri.keel.core.logger;

import io.github.sinri.keel.Keel;
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
@Deprecated(since = "2.6", forRemoval = true)
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
        String prefix;
        if (this.categoryPrefix != null && !this.categoryPrefix.isEmpty()) {
            prefix = this.categoryPrefix;
        } else {
            prefix = aspectComponentList.get(aspectComponentList.size() - 1);
        }

        String currentDateExpression = getCurrentDateExpression(this.options.rotate);
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

        String realPath = dir.getAbsolutePath();

        String currentDateExpression = getCurrentDateExpression(this.options.archive);
        if (currentDateExpression != null) {
            realPath += File.separator + currentDateExpression;
            dir = new File(realPath);
        }

        realPath += File.separator + computeFileName();

        File file = new File(realPath);
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                new KeelLogger().warning("Cannot MKDIRS: " + dir.getAbsolutePath());
            }
        }

        return file;
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

        String threadInfo = "";
        if (this.options.isShowThreadID()) {
            threadInfo = "[" + Thread.currentThread().getId() + "] ";
        }
        String verticleDeploymentInfo = "";
        if (this.options.isShowVerticleDeploymentID()) {
            verticleDeploymentInfo = "{" + Keel.getVertx().getOrCreateContext().deploymentID() + "} ";
        }

        String meta = getCurrentDateExpression("yyyy-MM-dd HH:mm:ss.SSS") + " "
                + "[" + level.name() + "] "
                + "<" + subject + "> "
                + threadInfo
                + verticleDeploymentInfo;

        String content;
        if (this.options.getCompositionStyle() == KeelLoggerOptions.CompositionStyle.TWO_LINES) {
            content = meta + "\n" + msg;
            if (context != null) {
                content += " | " + context;
            }
        } else if (this.options.getCompositionStyle() == KeelLoggerOptions.CompositionStyle.THREE_LINES) {
            content = meta + "\n" + msg;
            if (context != null) {
                content += "\ncontext: " + context;
            }
        } else {
            // as ONE_LINE
            content = meta + msg;
            if (context != null) {
                content += " | " + context;
            }
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

    /**
     * @param level     KeelLogLevel
     * @param msg       Message String
     * @param throwable Throwable
     * @since 2.2 capsulized here from KeelLogger
     */
    public void exception(KeelLogLevel level, String msg, Throwable throwable) {
        if (level.isSilent() || level.isNegligibleThan(this.options.getLowestLevel())) {
            return;
        }
        if (throwable == null) {
            log(KeelLogLevel.ERROR, msg, null);
            return;
        }
        String prefix = throwable.getClass().getName() + " : " + throwable.getMessage();
        if (msg != null && !msg.isEmpty()) {
            prefix = msg + " × " + throwable.getClass().getName() + " : " + throwable.getMessage();
        }
        log(KeelLogLevel.ERROR, prefix, null);

        var lastThrowable = throwable;
        while (lastThrowable.getCause() != null) {
            var cause = lastThrowable.getCause();
            this.print(level, "Caused by " + cause.getClass().getName() + " : " + cause.getMessage(), null);
            lastThrowable = cause;
        }

        String currentIgnorablePackage = null;
        int currentIgnorablePackageLines = 0;
        for (var s : lastThrowable.getStackTrace()) {
            String ignorablePackage = this.options.verifyIgnorableThrowableStackPackage(s.getClassName());
            if (ignorablePackage != null) {
                if (currentIgnorablePackage == null) {
                    currentIgnorablePackage = ignorablePackage;
                    currentIgnorablePackageLines = 1;
//                    System.out.println("ignorablePackage: "+ignorablePackage+" A");
                } else if (currentIgnorablePackage.equals(ignorablePackage)) {
                    currentIgnorablePackageLines += 1;
//                    System.out.println("ignorablePackage: "+ignorablePackage+" B");
                } else {
                    // print current
                    this.print(level, "\t [*" + currentIgnorablePackageLines + "] " + currentIgnorablePackage, null);
                    currentIgnorablePackage = ignorablePackage;
                    currentIgnorablePackageLines = 1;
//                    System.out.println("ignorablePackage: "+ignorablePackage+" C");
                }
            } else {
                if (currentIgnorablePackage != null) {
                    this.print(level, "\t [*" + currentIgnorablePackageLines + "] " + currentIgnorablePackage, null);
                    currentIgnorablePackage = null;
//                    System.out.println("break: "+currentIgnorablePackage+" D");
                }
                this.print(level, "\t" + s, null);
            }
        }
        if (currentIgnorablePackage != null) {
            this.print(level, "\t [*" + currentIgnorablePackageLines + "] " + currentIgnorablePackage, null);
//            System.out.println("fin: "+currentIgnorablePackage);
        }
    }
}
