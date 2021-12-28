package io.github.sinri.keel.servant;

public class KeelQueueNoTaskPendingSituation extends Exception {
    public KeelQueueNoTaskPendingSituation() {
        super();
    }

    public KeelQueueNoTaskPendingSituation(String message) {
        super(message);
    }

    public KeelQueueNoTaskPendingSituation(String message, Throwable cause) {
        super(message, cause);
    }

    public KeelQueueNoTaskPendingSituation(Throwable cause) {
        super(cause);
    }
}
