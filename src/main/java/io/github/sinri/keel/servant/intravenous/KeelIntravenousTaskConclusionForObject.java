package io.github.sinri.keel.servant.intravenous;

/**
 * @since 2.8
 */
class KeelIntravenousTaskConclusionForObject implements KeelIntravenousTaskConclusion<Object> {
    private final String taskReference;
    private final boolean done;
    private String feedback = null;
    private Object result = null;

    public KeelIntravenousTaskConclusionForObject(String taskReference, boolean done) {
        this.taskReference = taskReference;
        this.done = done;
    }

    @Override
    public String getTaskReference() {
        return taskReference;
    }

    @Override
    public boolean isDone() {
        return done;
    }

    @Override
    public String getFeedback() {
        return feedback;
    }

    public KeelIntravenousTaskConclusionForObject setFeedback(String feedback) {
        this.feedback = feedback;
        return this;
    }

    @Override
    public Object getResult() {
        return result;
    }

    public KeelIntravenousTaskConclusionForObject setResult(Object result) {
        this.result = result;
        return this;
    }
}
