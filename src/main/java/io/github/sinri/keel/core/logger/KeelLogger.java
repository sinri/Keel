package io.github.sinri.keel.core.logger;

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
    protected String aspect = "default";
    protected KeelLogLevel lowestLevel = KeelLogLevel.INFO;
    protected String rotateDateTimeFormat = "yyyyMMdd";
    protected boolean keepWriterReady = true;
    protected File readyFile = null;
    protected BufferedWriter readyWriter = null;
    protected boolean showThreadID = true;

    public KeelLogger(File logRootDirectory, String aspect) {
        String[] aspectParts = aspect.split("[/\\\\]+");
        List<String> list = new ArrayList<>();
        for (var x : aspectParts) {
            if (x == null || x.trim().equalsIgnoreCase("")) continue;
            list.add(x);
        }
        if (list.size() > 1) {
            for (int i = 0; i < list.size() - 1; i++) {
                this.logRootDirectory = new File(logRootDirectory.getAbsolutePath() + File.separator + list.get(i));
            }
            this.aspect = aspectParts[list.size() - 1];
        } else {
            this.logRootDirectory = logRootDirectory;
            this.aspect = aspect;
        }
    }

    public KeelLogger(String aspect) {
        this.aspect = aspect;
    }

    public KeelLogger(File logRootDirectory) {
        this.logRootDirectory = logRootDirectory;
    }

    public KeelLogger() {
    }

    /**
     * @return a silent logger
     * @since 1.10
     */
    public static KeelLogger buildSilentLogger() {
        return new KeelLogger().setLowestLevel(KeelLogLevel.SILENT);
    }

    /**
     * FORMAT: "yyyyMMdd" or "yyyy-MM-dd HH:mm:ss", etc.
     */
    public static String getCurrentDateExpression(String format) {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(currentTime);
    }

    public String getRotateDateTimeFormat() {
        return rotateDateTimeFormat;
    }

    /**
     * @param rotateDateTimeFormat FORMAT: "yyyyMMdd" or "yyyy-MM-dd HH:mm:ss", etc.
     */
    public KeelLogger setRotateDateTimeFormat(String rotateDateTimeFormat) {
        this.rotateDateTimeFormat = rotateDateTimeFormat;
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
        this.keepWriterReady = keepWriterReady;
        if (!this.keepWriterReady && readyWriter != null) {
            try {
                readyWriter.close();
            } catch (IOException e) {
                //e.printStackTrace();
                System.err.println(e.getMessage());
            }
        }
        return this;
    }

    protected File getOutputTargetFile() throws IOException {
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
        File aspectDir = new File(logRootDirectory.getAbsolutePath() + File.separator + aspect);
        if (!aspectDir.exists()) {
            if (!aspectDir.mkdirs()) {
                throw new IOException("Cannot MKDIRS: " + aspectDir.getAbsolutePath());
            }
        }

        String realPath = aspectDir.getAbsolutePath()
                + File.separator
                + aspect + "-" + getCurrentDateExpression(rotateDateTimeFormat) + ".log";

        if (keepWriterReady) {
            if (readyFile == null || !readyFile.getAbsolutePath().equals(realPath)) {
                readyFile = new File(realPath);
            }
            return readyFile;
        } else {
            return new File(realPath);
        }
    }

    public void log(KeelLogLevel level, String msg, JsonObject context) {
        if (level.isSilent() || !level.isMoreSeriousThan(lowestLevel)) {
            return;
        }
        String content = getCurrentDateExpression("yyyy-MM-dd HH:mm:ss") + " "
                + "[" + level.name() + "] "
                + "<" + aspect + "> "
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
        if (logRootDirectory == null) {
            System.out.println(content);
        } else {
            if (keepWriterReady) {
                try {
                    if (readyWriter == null) {
                        readyWriter = new BufferedWriter(new FileWriter(getOutputTargetFile(), true));
                    }
                    readyWriter.write(content);
                    readyWriter.newLine();
                    readyWriter.flush();
                } catch (IOException e) {
                    //e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            } else {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(getOutputTargetFile(), true))) {
                    bw.write(content);
                    bw.newLine();
                } catch (IOException e) {
                    // e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    /**
     * It would always print the content to STDOUT, but any occurred errors would be printed to STDERR.
     *
     * @param content the String content
     * @param ending  the String ending
     */
    public void print(String content, String ending) {
        if (logRootDirectory == null) {
            System.out.print(content);
            System.out.print(ending);
        } else {
            if (keepWriterReady) {
                try {
                    if (readyWriter == null) {
                        readyWriter = new BufferedWriter(new FileWriter(getOutputTargetFile(), true));
                    }
                    readyWriter.write(content);
                    readyWriter.write(ending);
                    readyWriter.flush();
                } catch (IOException e) {
                    //e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            } else {
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(getOutputTargetFile(), true))) {
                    bw.write(content);
                    bw.write(ending);
                } catch (IOException e) {
                    // e.printStackTrace();
                    System.err.println(e.getMessage());
                }
            }
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
