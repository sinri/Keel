package io.github.sinri.keel.facade.async;

import io.vertx.core.Future;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * 让同一个异步任务按照设定连续运行一定的次数。
 *
 * @since 1.13
 */
public class FutureForRange {
    private final Options options;

    private FutureForRange(Options options) {
        this.options = options;
    }

    /**
     * @since 2.9
     */
    static Future<Void> call(Options options, Function<Integer, Future<Void>> handleFunction) {
        return new FutureForRange(options).run(handleFunction);
    }

    /**
     * @param times since 2.9 changed to int from Integer
     * @since 2.9
     */
    static Future<Void> call(int times, Function<Integer, Future<Void>> handleFunction) {
        Options options = new Options().setEnd(times);
        return new FutureForRange(options).run(handleFunction);
    }

    /**
     * @since 2.9 use FutureUntil to avoid Thread Blocking Issue.
     */
    private Future<Void> run(Function<Integer, Future<Void>> handleFunction) {
        AtomicInteger indexRef = new AtomicInteger(options.getStart());

        return KeelAsyncKit.repeatedlyCall(routineResult -> {
            if (indexRef.get() < options.getEnd()) {
                return handleFunction.apply(indexRef.get())
                        .compose(v -> {
                            indexRef.addAndGet(options.getStep());
                            return Future.succeededFuture();
                        });
            } else {
                routineResult.stop();
                return Future.succeededFuture();
            }
        });
    }

    public static class Options {
        private int start;
        private int end;
        private int step;

        public Options() {
            this.start = 0;
            this.end = 0;
            this.step = 1;
        }

        public int getStart() {
            return start;
        }

        public Options setStart(int start) {
            this.start = start;
            return this;
        }

        public int getEnd() {
            return end;
        }

        public Options setEnd(int end) {
            this.end = end;
            return this;
        }

        public int getStep() {
            return step;
        }

        public Options setStep(int step) {
            this.step = step;
            return this;
        }


    }
}
