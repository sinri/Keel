package io.github.sinri.keel.test.servant;

import io.github.sinri.keel.Keel;
import io.github.sinri.keel.servant.KeelServant;
import io.vertx.core.VertxOptions;

public class ServantTest1 {

    public static void main(String[] args) throws InterruptedException {
        Keel.initializeVertx(new VertxOptions().setWorkerPoolSize(2));
        Keel.loadPropertiesFromFile("test.properties");

        KeelServant servant = createServant();
        calling();

        Thread.sleep(1000);

        servant.unregister();

        Thread.sleep(1000);

        Keel.getVertx().close();
    }

    private static KeelServant createServant() {
        KeelServant a = new KeelServant(Keel.getVertx(), "a");
        a.handleString(x -> {
            System.out.println("a handling: " + x);
            return "[done] " + a;
        });
        return a;
    }

    private static void calling() {
        for (int i = 0; i < 10; i++) {
            Keel.getEventBus().publish("a", "main i=" + i);
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            (new MessageProductThread(i)).start();
        }
    }

    public static class MessageProductThread extends Thread {
        int thread_id;

        public MessageProductThread(int thread_id) {
            this.thread_id = thread_id;
        }

        @Override
        public void run() {
            super.run();

            for (int i = 0; i < 10; i++) {
                Keel.getEventBus().publish("a", "MessageProductThread[" + thread_id + "] i=" + i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
