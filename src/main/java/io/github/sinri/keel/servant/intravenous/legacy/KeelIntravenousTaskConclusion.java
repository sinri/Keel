package io.github.sinri.keel.servant.intravenous.legacy;

/**
 * @param <R>
 * @since 2.7
 */
@Deprecated(since = "2.9", forRemoval = true)
public interface KeelIntravenousTaskConclusion<R> {

    /**
     * @since 2.8
     */
    static <R> KeelIntravenousTaskConclusion<R> create(String reference, boolean done, String feedback, R result) {
        return new KeelIntravenousTaskConclusion<R>() {
            @Override
            public String getTaskReference() {
                return reference;
            }

            @Override
            public boolean isDone() {
                return done;
            }

            @Override
            public String getFeedback() {
                return feedback;
            }

            @Override
            public R getResult() {
                return result;
            }
        };
    }

    /**
     * @since 2.8
     */
    static KeelIntravenousTaskConclusion<Object> createForObject(String reference, boolean done) {
        return new KeelIntravenousTaskConclusionForObject(reference, done);
    }

    /**
     * @since 2.8
     */
    static KeelIntravenousTaskConclusion<Object> createForObject(String reference, boolean done, String feedback, Object result) {
        return new KeelIntravenousTaskConclusionForObject(reference, done)
                .setFeedback(feedback)
                .setResult(result);
    }

    String getTaskReference();

    boolean isDone();

    default String getFeedback() {
        return "";
    }

    default R getResult() {
        return null;
    }
}
