package io.github.sinri.keel.tesuto;

import javax.annotation.Nonnull;

/**
 * @since 3.0.14 add skip.
 */
public class TestUnitResult {
    private final @Nonnull String testName;
    private Long spentTime;
    private Boolean done;
    private Throwable cause;
    private Boolean skipped;

    public TestUnitResult(@Nonnull String name) {
        this.testName = name;
    }

    public void declareDone() {
        this.done = true;
    }

    public void declareFailed(Throwable cause) {
        this.done = false;
        this.cause = cause;
        this.skipped = false;
    }

    public void declareSkipped() {
        this.skipped = true;
    }

    @Nonnull
    public String getTestName() {
        return testName;
    }

    public Long getSpentTime() {
        return spentTime;
    }

    public TestUnitResult setSpentTime(Long spentTime) {
        this.spentTime = spentTime;
        return this;
    }

    public boolean isDone() {
        return done != null && done;
    }

    public boolean isFailed() {
        return done != null && !done;
    }


    public Throwable getCause() {
        return cause;
    }

    public boolean isSkipped() {
        return skipped != null && skipped;
    }
}
