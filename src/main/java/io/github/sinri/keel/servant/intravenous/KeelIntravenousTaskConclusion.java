package io.github.sinri.keel.servant.intravenous;

/**
 * @param <R>
 * @since 2.7
 */
public interface KeelIntravenousTaskConclusion<R> {

    String getTaskReference();

    boolean isDone();

    default String getFeedback() {
        return "";
    }

    default R getResult() {
        return null;
    }
}
