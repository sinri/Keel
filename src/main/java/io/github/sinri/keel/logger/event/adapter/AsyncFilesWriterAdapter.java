package io.github.sinri.keel.logger.event.adapter;

import io.github.sinri.keel.facade.async.KeelAsyncKit;
import io.github.sinri.keel.helper.KeelHelpers;
import io.github.sinri.keel.logger.event.KeelEventLog;
import io.vertx.core.Future;
import io.vertx.core.Promise;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

/**
 * @since 3.0.0
 */
public class AsyncFilesWriterAdapter implements KeelEventLoggerAdapter {
    private final String logDir;
    private final String dateFormat;
    private final Queue<KeelEventLog> queue;
    private final Function<KeelEventLog, String> eventLogComposer;

    public AsyncFilesWriterAdapter(String logDir) {
        this(logDir, "yyyy-MM-dd", null);
    }

    public AsyncFilesWriterAdapter(String logDir, String dateFormat) {
        this(logDir, dateFormat, null);
    }

    public AsyncFilesWriterAdapter(String logDir, String dateFormat, Function<KeelEventLog, String> eventLogComposer) {
        this.logDir = logDir;
        this.dateFormat = dateFormat;
        this.eventLogComposer = eventLogComposer;
        this.queue = new ConcurrentLinkedQueue<>();
    }


    @Override
    public void close(Promise<Void> promise) {
        promise.complete();
    }

    @Override
    public Future<Void> dealWithLogs(List<KeelEventLog> buffer) {
        Map<String, List<KeelEventLog>> fileLogsMap = new HashMap<>();

        for (KeelEventLog eventLog : buffer) {
            try {
                String topic = eventLog.topic();
                String[] topicComponents = topic.replaceAll("(^[.]+)|([.]+$)", "").split("[.]+");

                String finalTopic;
                File finalDir;
                if (topicComponents.length > 1) {
                    finalTopic = topicComponents[topicComponents.length - 1];
                    StringBuilder x = new StringBuilder(this.logDir);
                    for (int i = 0; i < topicComponents.length - 1; i++) {
                        x.append(File.separator).append(topicComponents[i]);
                    }
                    finalDir = new File(x.toString());
                } else {
                    finalTopic = topic;
                    finalDir = new File(this.logDir + File.separator + topic);
                }


                if (!finalDir.exists()) {
                    if (!finalDir.mkdirs()) {
                        throw new IOException("Path " + finalDir + " create dir failed");
                    }
                }
                if (!finalDir.isDirectory()) {
                    throw new IOException("Path " + finalDir + " not dir");
                }
                String finalFile = finalDir + File.separator + finalTopic + "-"
                        + KeelHelpers.datetimeHelper().getDateExpression(new Date(eventLog.timestamp()), dateFormat)
                        + ".log";

                fileLogsMap.computeIfAbsent(finalFile, s -> new ArrayList<>()).add(eventLog);

            } catch (Throwable e) {
                System.out.println("AsyncFilesWriterAdapter::dealWithLogs ERROR: " + e);
            }
        }

        return KeelAsyncKit.parallelForAllResult(fileLogsMap.entrySet(), entry -> {
                    return dealWithLogsForOneFile(new File(entry.getKey()), entry.getValue());
                })
                .compose(parallelResult -> {
                    return Future.succeededFuture();
                });
    }

    private Future<Void> dealWithLogsForOneFile(File file, List<KeelEventLog> buffer) {
        try (FileWriter fileWriter = new FileWriter(file, true)) {
            for (KeelEventLog eventLog : buffer) {
                if (this.eventLogComposer == null) {
                    fileWriter.write(eventLog.toString() + "\n");
                } else {
                    fileWriter.write(this.eventLogComposer.apply(eventLog) + "\n");
                }
            }
        } catch (IOException e) {
            System.out.println("AsyncFilesWriterAdapter::dealWithLogsForOneFile(" + file + ") ERROR: " + e);
        }
        return Future.succeededFuture();
    }

    @Override
    public Object processThrowable(Throwable throwable) {
        return KeelHelpers.stringHelper().renderThrowableChain(throwable);
    }
}
