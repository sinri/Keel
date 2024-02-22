package io.github.sinri.keel.logger.event.legacy.adapter;

import io.github.sinri.keel.logger.event.legacy.KeelEventLog;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static io.github.sinri.keel.helper.KeelHelpersInterface.KeelHelpers;

public class SingleFileWriterAdapter implements KeelEventLoggerAdapter {
    private FileWriter fileWriter;

    public SingleFileWriterAdapter(String filepath) {
        try {
            fileWriter = new FileWriter(filepath, true);
        } catch (IOException e) {
            fileWriter = null;
        }
    }

    @Override
    public void close(@Nonnull Promise<Void> promise) {
        try {
            fileWriter.close();
            promise.complete();
        } catch (IOException e) {
            promise.fail(e);
        } finally {
            fileWriter = null;
        }
    }

    @Override
    @Nonnull
    public Future<Void> dealWithLogs(@Nonnull List<KeelEventLog> buffer) {
        if (fileWriter != null) {
            buffer.forEach(eventLog -> {
                try {
                    fileWriter.write(eventLog.toString());
                    fileWriter.write("\n");
                } catch (IOException e) {
                    // ignore
                }
            });
        }
        return Future.succeededFuture();
    }

    @Override
    @Nullable
    public Object processThrowable(@Nullable Throwable throwable) {
        return KeelHelpers.stringHelper().renderThrowableChain(throwable);
    }

}
