package io.github.sinri.keel.test.annotation;

public class Worker {

    @AnnoI(fieldName = "a")
    public void work() {
        System.out.println();
    }
}
