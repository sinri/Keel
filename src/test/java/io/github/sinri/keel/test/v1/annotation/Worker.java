package io.github.sinri.keel.test.v1.annotation;

public class Worker {

    public static void main(String[] args) {

        try {
            Worker worker = new Worker();
            AnnoI annotation = null;
            annotation = Worker.class.getMethod("work").getAnnotation(AnnoI.class);
            System.out.println("annotation.fieldName() = " + annotation.fieldName());

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }

    @AnnoI(fieldName = "a")
    public void work() {

        System.out.println();
    }
}
