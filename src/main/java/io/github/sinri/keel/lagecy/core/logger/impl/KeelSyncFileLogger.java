package io.github.sinri.keel.lagecy.core.logger.impl;

import io.github.sinri.keel.lagecy.core.logger.AbstractKeelFileLogger;
import io.github.sinri.keel.lagecy.core.logger.KeelLogger;
import io.github.sinri.keel.lagecy.core.logger.KeelLoggerOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class KeelSyncFileLogger extends AbstractKeelFileLogger {

    protected String readyWriterFilePath;
    protected BufferedWriter readyWriter;

    public KeelSyncFileLogger(KeelLoggerOptions keelLoggerOptions) {
        super(keelLoggerOptions);
    }

    @Override
    public KeelLogger setCategoryPrefix(String categoryPrefix) {
        super.setCategoryPrefix(categoryPrefix);

        resetReadyWriter();

        return this;
    }

    protected synchronized void resetReadyWriter() {
        if (this.readyWriter != null) {
            try {
                this.readyWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.readyWriter = null;
        this.readyWriterFilePath = null;
    }

    protected synchronized BufferedWriter getWriter() {
        File outputTargetFile = this.computeFile();
        if (outputTargetFile == null) {
            return null;
        }
        if (options.shouldKeepWriterReady()) {
            if (
                    outputTargetFile.getAbsolutePath().equals(this.readyWriterFilePath)
                            && this.readyWriter != null
            ) {
                return this.readyWriter;
            } else {
                try {
                    this.readyWriter = new BufferedWriter(new FileWriter(outputTargetFile, true));
                    this.readyWriterFilePath = outputTargetFile.getAbsolutePath();
                    return this.readyWriter;
                } catch (IOException e) {
                    this.readyWriterFilePath = null;
                    return null;
                }
            }
        } else {
            try {
                return new BufferedWriter(new FileWriter(outputTargetFile, this.options.getFileOutputCharset(), true));
            } catch (IOException ignored) {
                return null;
            }
        }
    }

    @Override
    public void text(String text) {
        BufferedWriter writer = this.getWriter();
        if (writer == null) {
            System.out.print(text);
        } else {
            try {
                writer.write(text);
                writer.flush();
                if (!this.options.shouldKeepWriterReady()) {
                    writer.close();
                }
            } catch (IOException e) {
                System.out.print(text);
                e.printStackTrace();
            }
        }
    }

}
