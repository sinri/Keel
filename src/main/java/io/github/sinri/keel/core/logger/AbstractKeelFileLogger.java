package io.github.sinri.keel.core.logger;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.core.logger.impl.KeelPrintLogger;

import java.io.File;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This abstract class level is focusing on which FILE to write into.
 *
 * @since 2.6
 */
abstract public class AbstractKeelFileLogger extends AbstractKeelLogger {
    private Function<AbstractKeelFileLogger, File> fileComputeFunction = null;

    public AbstractKeelFileLogger(KeelLoggerOptions options) {
        super(options);
    }

    /**
     * From Aspect and Category Prefix
     * Aspect: a | a/b
     * Category Prefix: EMPTY | x
     *
     * @return computed Relative Directory Path
     */
    protected static String computeRelativeDirPath(KeelLoggerOptions options) {
        StringBuilder relativePath = new StringBuilder(
                Keel.helpers().string()
                        .joinStringArray(
                                options.getAspectComponentList(),
                                File.separator
                        )
        );

        Pattern pattern = Pattern.compile("\\{([A-Za-z: _.]+)}");
        if (options.getArchivePath() != null) {
            String[] parts = options.getArchivePath().split("/+");
            for (String part : parts) {
                if (part == null || part.trim().isEmpty()) {
                    continue;
                }
                Matcher matcher = pattern.matcher(part);
                String parsedPart = matcher.replaceAll(matchResult -> {
                    String format = matchResult.group(1);
                    return Keel.helpers().datetime().getCurrentDateExpression(format);
                });
                relativePath.append(File.separator).append(parsedPart);
            }
        }

        return relativePath.toString();
    }

    protected static String computeFileName(String subject, String categoryPrefix, String fileRotateFormat) {
        String prefix;
        if (categoryPrefix != null && !categoryPrefix.isEmpty()) {
            prefix = categoryPrefix;
        } else {
            prefix = subject;
        }

        String currentDateExpression = Keel.helpers().datetime().getCurrentDateExpression(fileRotateFormat);
        if (currentDateExpression != null) {
            return prefix + "-" + currentDateExpression + ".log";
        } else {
            return prefix + ".log";
        }

    }

    /**
     * @return File or null
     */
    protected File computeFile() {
        if (this.fileComputeFunction != null) {
            return this.fileComputeFunction.apply(this);
        }

        File logRootDirectory = this.options.getDir();
        if (logRootDirectory == null) {
            // would be null -> directly output to stdout
            new KeelPrintLogger(options).warning("Log Root Directory is not valid");
            return null;
        }

        if (logRootDirectory.exists()) {
            if (logRootDirectory.isFile()) {
                // directly output to the file `logRootDirectory`
                return logRootDirectory;
            }
        }

        File dir = new File(logRootDirectory.getAbsolutePath() + File.separator + computeRelativeDirPath(options));

        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                new KeelPrintLogger(options).warning("Cannot MKDIRS: " + dir.getAbsolutePath());
            }
        }

        String realPath = dir.getAbsolutePath() + File.separator + computeFileName(options.getSubject(), getCategoryPrefix(), options.getFileRotateFormat());

        return new File(realPath);
    }

    /**
     * @since 2.6
     */
    public void setFileComputeFunction(Function<AbstractKeelFileLogger, File> func) {
        this.fileComputeFunction = func;
    }
}
