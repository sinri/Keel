package io.github.sinri.keel.queue;

abstract public class KeelSyncQueueTask implements Runnable {

    abstract public String getTaskReference();

    @Override
    public final void run() {
        // RUN IN WORKER THREAD
        try {
            String feedback = runTask();
            declareTaskFinished(true, feedback);
        } catch (Exception e) {
            declareTaskFinished(false, e.getMessage());
        }
    }

    /**
     * RUN IN MASTER THREAD
     *
     * @return if successfully declared
     */
    abstract public boolean declareTaskToRun();

    /**
     * RUN IN WORKER THREAD
     *
     * @throws Exception if task met any error
     */
    abstract public String runTask() throws Exception;

    /**
     * RUN IN WORKER THREAD
     *
     * @param isDone   DONE or ERROR
     * @param feedback the feedback content
     */
    abstract public void declareTaskFinished(boolean isDone, String feedback);
}
