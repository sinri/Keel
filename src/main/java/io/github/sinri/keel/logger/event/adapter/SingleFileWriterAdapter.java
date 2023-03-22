package io.github.sinri.keel.logger.event.adapter;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class SingleFileWriterAdapter implements KeelEventLoggerAdapter {
    private Writer fileWriter;

    public SingleFileWriterAdapter(String filepath) {
        try {
            fileWriter = Files.newBufferedWriter(Paths.get(filepath), UTF_8, CREATE, APPEND);
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
