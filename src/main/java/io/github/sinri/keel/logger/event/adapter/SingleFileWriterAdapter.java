package io.github.sinri.keel.logger.event.adapter;

import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

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
    public void close(Promise<Void> promise) {
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
    public Future<Void> dealWithLogs(List<KeelEventLog> buffer) {
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
    public Object processThrowable(Throwable throwable) {
        return KeelHelpers.stringHelper().renderThrowableChain(throwable);
    }

}
