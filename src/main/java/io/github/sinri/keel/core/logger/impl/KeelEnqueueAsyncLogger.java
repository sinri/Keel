package io.github.sinri.keel.core.logger.impl;

import io.github.sinri.keel.core.logger.AbstractKeelLogger;
import io.github.sinri.keel.core.logger.KeelLoggerOptions;

import java.util.Queue;

/**
 * 将日志内容组装成String后加入指定队列。
 * 配合异步servant使用（KeelEndless、KeelInjection等），由外部servant来读取队列。
 *
 * @since 2.8
 */
public class KeelEnqueueAsyncLogger extends AbstractKeelLogger {
    private final Queue<String> queue;

    /**
     * @param queue It must be a Thread-Safe Queue instance.
     */
    public KeelEnqueueAsyncLogger(KeelLoggerOptions options, Queue<String> queue) {
        super(options);
        this.queue = queue;
    }

    @Override
    public void text(String text) {
        this.queue.offer(text);
    }
}
