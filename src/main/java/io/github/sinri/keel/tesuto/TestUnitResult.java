package io.github.sinri.keel.tesuto;

public class TestUnitResult {
    private final String testName;
    private Long spentTime;
    private Boolean done;
    private Throwable cause;

    public TestUnitResult(String name) {
        this.testName = name;
    }

    public void declareDone() {
        this.done = true;
    }

    public void declareFailed(Throwable cause) {
        this.done = false;
        this.cause = cause;
    }

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

    public Boolean isFailed() {
        return !done;
    }


    public Throwable getCause() {
        return cause;
    }
}
