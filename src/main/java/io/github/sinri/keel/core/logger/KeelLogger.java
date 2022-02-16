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

/**
 * The Logger For Keel Project
 */
public class KeelLogger {

    protected File logRootDirectory = null;
    protected List<String> aspectComponentList = new ArrayList<>();
    protected String categoryPrefix = null;
    protected KeelLogLevel lowestLevel = KeelLogLevel.INFO;
    protected String rotateDateTimeFormat = "yyyyMMdd";
    protected boolean keepWriterReady = true;
    protected File readyFile = null;
    protected BufferedWriter readyWriter = null;
    protected boolean showThreadID = true;

    public KeelLogger(File logRootDirectory, String aspect, String categoryPrefix) {
        this.logRootDirectory = logRootDirectory;
        this.parseAspect(aspect);
        this.categoryPrefix = categoryPrefix;
    }

    public KeelLogger(File logRootDirectory, String aspect) {
        this.logRootDirectory = logRootDirectory;
        this.parseAspect(aspect);
    }

    public KeelLogger(String aspect) {
        this.parseAspect(aspect);
    }

    public KeelLogger(File logRootDirectory) {
        this.logRootDirectory = logRootDirectory;
        this.parseAspect("");
    }

    public KeelLogger() {
        this.parseAspect("");
    }

    /**
     * @return a silent logger
     * @since 1.10
     */
    public static KeelLogger buildSilentLogger() {
        return new KeelLogger().setLowestLevel(KeelLogLevel.SILENT);
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

    protected void parseAspect(String aspect) {
        String[] aspectParts = aspect.split("[/\\\\]+");
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
    public KeelLogger setCategoryPrefix(String categoryPrefix) {
        this.categoryPrefix = categoryPrefix;
        if (this.keepWriterReady) {
            resetReadyWriter();
        }
        return this;
    }

    public String getRotateDateTimeFormat() {
        return rotateDateTimeFormat;
    }

    /**
     * @param rotateDateTimeFormat FORMAT: "yyyyMMdd" or "yyyy-MM-dd HH:mm:ss", etc.
     */
    public KeelLogger setRotateDateTimeFormat(String rotateDateTimeFormat) {
        this.rotateDateTimeFormat = rotateDateTimeFormat;
        if (this.keepWriterReady) {
            resetReadyWriter();
        }
        return this;
    }

    public KeelLogLevel getLowestLevel() {
        return lowestLevel;
    }

    public KeelLogger setLowestLevel(KeelLogLevel lowestLevel) {
        this.lowestLevel = lowestLevel;
        return this;
    }

    /**
     * @param showThreadID a boolean, whether the log descriptor should prompt the thread ID
     * @return this
     * @since 1.5
     */
    public KeelLogger setShowThreadID(boolean showThreadID) {
        this.showThreadID = showThreadID;
        return this;
    }

    public boolean isKeepWriterReady() {
        return keepWriterReady;
    }

    public KeelLogger setKeepWriterReady(boolean keepWriterReady) {
        if (this.keepWriterReady != keepWriterReady) {
            resetReadyWriter();
        }
        this.keepWriterReady = keepWriterReady;
        return this;
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
        String currentDateExpression = getCurrentDateExpression(rotateDateTimeFormat);
        if (currentDateExpression != null) {
            return prefix + "-" + currentDateExpression + ".log";
        } else {
            return prefix + ".log";
        }
    }

    protected File getOutputTargetFile() {
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
                new KeelLogger("KeelLogger").warning("Cannot MKDIRS: " + dir.getAbsolutePath());
                //throw new IOException("Cannot MKDIRS: " + dir.getAbsolutePath());
            }
        }

        String realPath = dir.getAbsolutePath() + File.separator + computeFileName();
        // TODO remove debug
        System.out.println("getOutputTargetFile realPath: " + realPath);

        return new File(realPath);
    }

    protected BufferedWriter getWriter(File outputTargetFile) throws IOException {
        if (keepWriterReady) {
            if (readyWriter != null) {
                if (readyFile == null || !outputTargetFile.getAbsolutePath().equals(readyFile.getAbsolutePath())) {
                    resetReadyWriter();
                }
            }
            if (readyWriter == null) {
                readyWriter = new BufferedWriter(new FileWriter(outputTargetFile, true));
                readyFile = outputTargetFile;
            }
            return readyWriter;
        } else {
            return new BufferedWriter(new FileWriter(outputTargetFile, true));
        }
    }

    protected void resetReadyWriter() {
        try {
            if (readyWriter != null) {
                readyWriter.close();
            }
        } catch (IOException e) {
            new KeelLogger("KeelLogger").warning("Failed to close readyWriter: " + e.getMessage());
        } finally {
            readyWriter = null;
            readyFile = null;
        }
    }

    public void log(KeelLogLevel level, String msg, JsonObject context) {
        if (level.isSilent() || !level.isMoreSeriousThan(lowestLevel)) {
            return;
        }
        String subject = aspectComponentList.get(aspectComponentList.size() - 1);
        if (categoryPrefix != null && !categoryPrefix.isEmpty()) {
            subject += ":" + categoryPrefix;
        }
        String content = getCurrentDateExpression("yyyy-MM-dd HH:mm:ss") + " "
                + "[" + level.name() + "] "
                + "<" + subject + "> "
                + (showThreadID ? ("[" + Thread.currentThread().getId() + "] ") : "")
                + msg;
        if (context != null) {
            content += " | " + context;
        }
        print(content);
    }

    /**
     * It would always print the content to STDOUT, but any occurred errors would be printed to STDERR.
     *
     * @param content the String content
     */
    public void print(String content) {
        File outputTargetFile = getOutputTargetFile();
        if (outputTargetFile == null) {
            System.out.println(content);
        } else {
            try {
                BufferedWriter writer = getWriter(outputTargetFile);
                writer.write(content);
                writer.newLine();
                writer.flush();
                if (!keepWriterReady) {
                    writer.close();
                }
            } catch (IOException e) {
                new KeelLogger("KeelLogger").exception(e);
                System.out.println(content);
            }

//            if (keepWriterReady) {
//                try {
//                    if (readyWriter == null) {
//                        readyWriter = new BufferedWriter(new FileWriter(getOutputTargetFile(), true));
//                    }
//                    readyWriter.write(content);
//                    readyWriter.newLine();
//                    readyWriter.flush();
//                } catch (IOException e) {
//                    //e.printStackTrace();
//                    new KeelLogger().exception(e);
//                }
//            } else {
//                try (BufferedWriter bw = new BufferedWriter(new FileWriter(getOutputTargetFile(), true))) {
//                    bw.write(content);
//                    bw.newLine();
//                } catch (IOException e) {
//                    // e.printStackTrace();
////                    System.err.println(e.getMessage());
//                    new KeelLogger().exception(e);
//                }
//            }
        }
    }

    /**
     * It would always print the content to STDOUT, but any occurred errors would be printed to STDERR.
     *
     * @param content the String content
     * @param ending  the String ending
     */
    public void print(String content, String ending) {
        File outputTargetFile = getOutputTargetFile();
        if (logRootDirectory == null) {
            System.out.print(content);
            System.out.print(ending);
        } else {
            try {
                BufferedWriter writer = getWriter(outputTargetFile);
                writer.write(content);
                writer.write(ending);
                writer.flush();
                if (!keepWriterReady) {
                    writer.close();
                }
            } catch (IOException e) {
                new KeelLogger("KeelLogger").exception(e);
                System.out.print(content);
                System.out.print(ending);
            }

//            if (keepWriterReady) {
//                try {
//                    if (readyWriter == null) {
//                        readyWriter = new BufferedWriter(new FileWriter(getOutputTargetFile(), true));
//                    }
//                    readyWriter.write(content);
//                    readyWriter.write(ending);
//                    readyWriter.flush();
//                } catch (IOException e) {
//                    //e.printStackTrace();
////                    System.err.println(e.getMessage());
//                    new KeelLogger().exception(e);
//                }
//            } else {
//                try (BufferedWriter bw = new BufferedWriter(new FileWriter(getOutputTargetFile(), true))) {
//                    bw.write(content);
//                    bw.write(ending);
//                } catch (IOException e) {
//                    // e.printStackTrace();
////                    System.err.println(e.getMessage());
//                    new KeelLogger().exception(e);
//                }
//            }
        }
    }

    public void debug(String msg, JsonObject context) {
        log(KeelLogLevel.DEBUG, msg, context);
    }

    public void debug(String msg) {
        log(KeelLogLevel.DEBUG, msg, null);
    }

    public void info(String msg, JsonObject context) {
        log(KeelLogLevel.INFO, msg, context);
    }

    public void info(String msg) {
        log(KeelLogLevel.INFO, msg, null);
    }

    public void notice(String msg, JsonObject context) {
        log(KeelLogLevel.NOTICE, msg, context);
    }

    public void notice(String msg) {
        log(KeelLogLevel.NOTICE, msg, null);
    }

    public void warning(String msg, JsonObject context) {
        log(KeelLogLevel.WARNING, msg, context);
    }

    public void warning(String msg) {
        log(KeelLogLevel.WARNING, msg, null);
    }

    public void error(String msg, JsonObject context) {
        log(KeelLogLevel.ERROR, msg, context);
    }

    public void error(String msg) {
        log(KeelLogLevel.ERROR, msg, null);
    }

    public void fatal(String msg, JsonObject context) {
        log(KeelLogLevel.FATAL, msg, context);
    }

    public void fatal(String msg) {
        log(KeelLogLevel.FATAL, msg, null);
    }

    public void exception(Throwable throwable) {
        exception(null, throwable);
    }

    /**
     * @param msg       since 1.10, a prefix String msg is supported
     * @param throwable the Throwable to print its details
     * @since 1.10
     */
    public void exception(String msg, Throwable throwable) {
        String prefix;
        if (msg == null || msg.isEmpty()) {
            prefix = "";
        } else {
            prefix = msg + " × ";
        }
        error(prefix + throwable.getMessage(), new JsonObject().put("error_class", throwable.getClass().getName()));
        for (var s : throwable.getStackTrace()) {
            print("\t" + s.toString(), "\n");
        }
    }
}
